package hdxian.itemservice.repository.jpa;

import hdxian.itemservice.domain.Item;
import hdxian.itemservice.repository.ItemRepository;
import hdxian.itemservice.repository.ItemSearchCond;
import hdxian.itemservice.repository.ItemUpdateDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * V1 - use JPA
 * EntityManager
 * 스프링 통합에 의해 내부적으로 사용하는 DataSource, TransactionManager 등이 자동으로 설정됨.
 */

@Slf4j
@Repository
@Transactional // JPA executed in Transaction. (expect select)
public class JpaItemRepositoryV1 implements ItemRepository {

    private final EntityManager em;

    public JpaItemRepositoryV1(EntityManager em) {
        this.em = em;
    }

    @Override
    public Item save(Item item) {
        em.persist(item); // item을 DB에 insert하고 id값도 해당 객체에 넣어줌.
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item item = em.find(Item.class, itemId);
        item.setItemName(updateParam.getItemName());
        item.setPrice(updateParam.getPrice());
        item.setQuantity(updateParam.getQuantity());
        // 트랜잭션 종료 직전 시점에 update 쿼리가 자동으로 날아감.
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class, id);
        return Optional.ofNullable(item); // null이 담겨 리턴될 수도 있음
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        // dynamic query in plain JPA -> need to use jpql...
        String jpql = "select i from Item i";

        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        // check where condition
        if (StringUtils.hasText(itemName) || maxPrice != null)
            jpql += " where";

        // check itemName condition
        boolean andFlag = false;
        if (StringUtils.hasText(itemName)) {
            jpql += " i.itemName like concat('%', :itemName, '%')";
            andFlag = true;
        }

        // check maxPrice condition
        if (maxPrice != null) {
            if (andFlag)
                jpql += " and";

            jpql += " i.price <= :maxPrice";

        }

        log.info("[JpaItemRepositoryV1.findAll] generated jpql={}", jpql);

        TypedQuery<Item> query = em.createQuery(jpql, Item.class);
        if(StringUtils.hasText(itemName)) {
            query.setParameter("itemName", itemName);
        }

        if (maxPrice != null) {
            query.setParameter("maxPrice", maxPrice);
        }

        // query is TypedQuery<Item> -> already knows return type -> returns List<Item>
        return query.getResultList();
    }
}
