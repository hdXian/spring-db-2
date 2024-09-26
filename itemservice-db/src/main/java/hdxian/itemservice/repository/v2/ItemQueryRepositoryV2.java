package hdxian.itemservice.repository.v2;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import hdxian.itemservice.domain.Item;
import hdxian.itemservice.repository.ItemSearchCond;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static hdxian.itemservice.domain.QItem.*;

// using QueryDsl for complex query.
@Repository
public class ItemQueryRepositoryV2 {

    private final JPAQueryFactory queryFactory;

    public ItemQueryRepositoryV2(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        List<Item> result = queryFactory.select(item)
                .from(item)
                .where(
                        itemNameLike(itemName),
                        maxPriceLoe(maxPrice)
                )
                .fetch();

        return result;
    }

    private BooleanExpression maxPriceLoe(Integer maxPrice) {
        if (maxPrice != null) {
            return item.price.loe(maxPrice);
        }
        return null;
    }

    private BooleanExpression itemNameLike(String itemName) {
        if (StringUtils.hasText(itemName)) {
            return item.itemName.like("%" + itemName + "%");
        }
        return null;
    }

}
