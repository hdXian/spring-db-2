package hdxian.itemservice.repository.jpa;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hdxian.itemservice.domain.Item;
import hdxian.itemservice.domain.QItem;
import hdxian.itemservice.repository.ItemRepository;
import hdxian.itemservice.repository.ItemSearchCond;
import hdxian.itemservice.repository.ItemUpdateDto;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

import static hdxian.itemservice.domain.QItem.*;

/**
 * V3 - QueryDsl
 * 간단한 기능들은 JPA를 사용하고 (본인 선택), 동적 쿼리를 QueryDsl을 이용해 작성함.
 * QueryDsl은 jpql을 생성해주는 빌더로서, 결국 jpql을 생성해 내부적으로 JPA를 이용해 쿼리를 날림. 즉 EntityManager가 필요함.
 * QueryDsl을 이용하면 자바 코드를 통해 추상화된 쿼리를 만들어 쓰기 때문에 안전하게 쿼리를 작성할 수 있음.
 */

@Transactional
@Repository // QueryDsl은 자체적으로 스프링 예외 추상화를 지원하지 않기 때문에, @Repository를 붙여 예외 추상화 프록시를 적용해야 함.
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    // JdbcTemplate 생성자와 유사한 방식. (관례)
    public JpaItemRepositoryV3(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Item save(Item item) {
        em.persist(item); // 실행 후 item 객체에 id값까지 넣어줌.
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item item = em.find(Item.class, itemId);
        item.setItemName(updateParam.getItemName());
        item.setPrice(updateParam.getPrice());
        item.setQuantity(updateParam.getQuantity());
        // 트랜잭션 종료 시점에 객체들의 변경 사항을 파악하여 update 쿼리가 날아감.
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item); // null이 딤겨 리턴될 수도 있음.
    }

    public List<Item> findAll_Old(ItemSearchCond cond) {

        String itemNameCond = cond.getItemName();
        Integer maxPriceCond = cond.getMaxPrice();

        // 쿼리용 Item 객체 QItem
        QItem item = QItem.item;

        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.hasText(itemNameCond)) {
            builder.and(item.itemName.like("%" + itemNameCond + "%")); // jpql += (and i.itemName like %:itemNameCond%)
        }

        if (maxPriceCond != null) {
            builder.and(item.price.loe(maxPriceCond)); // jpql += (and i.price <= :maxPriceCond)
        }

        List<Item> result = queryFactory
                .select(item)
                .from(item)
                .where(builder)
                .fetch();

        return result;
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemNameCond = cond.getItemName();
        Integer maxPriceCond = cond.getMaxPrice();

        List<Item> result = queryFactory
                .select(item)
                .from(item)
                .where(likeItemName(itemNameCond), maxPrice(maxPriceCond))
                .fetch();

        return result;
    }

    // maxPrice 조건이 있을 경우 해당 조건을 추가한 표현식을 리턴.
    private BooleanExpression maxPrice(Integer maxPriceCond) {
        if (maxPriceCond != null) {
            return item.price.loe(maxPriceCond);
        }
        return null;
    }

    // itemName 조건이 있을 경우 해당 조건을 추가한 표현식을 리턴.
    private BooleanExpression likeItemName(String itemNameCond) {
        if (StringUtils.hasText(itemNameCond)) {
            return item.itemName.like("%" + itemNameCond + "%");
        }
        return null;
    }
}
