package hdxian.itemservice.repository.jpa;

import hdxian.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long> {

    // where item_name like :itemName
    List<Item> findByItemNameLike(String itemName);

    // where i.price <= :price
    List<Item> findByPriceLessThanEqual(Integer price);

    // findByItemNameLike + and + findByPriceLessThanEqual
    List<Item> findByItemNameLikeAndPriceLessThanEqual(String ItemName, Integer price);

    // same as above
    @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
    List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);

}
