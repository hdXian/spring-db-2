package hdxian.itemservice.repository.jpa;

import hdxian.itemservice.domain.Item;
import hdxian.itemservice.repository.ItemRepository;
import hdxian.itemservice.repository.ItemSearchCond;
import hdxian.itemservice.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Repository
public class JpaItemRepositoryV2 implements ItemRepository {

    private final SpringDataJpaItemRepository repository;

    @Override
    public Item save(Item item) {
        // JpaRepository 기본 제공 save() 메서드
        return repository.save(item);
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        // JpaRepository 기본 제공 findById() 메서드 -> JpaRepository<T, ID>의 ID 기준
        Item item = repository.findById(itemId).orElseThrow(); // Optional<T>에 값이 있으면 item에 리턴, 아니면 예외 throw
        item.setItemName(updateParam.getItemName());
        item.setPrice(updateParam.getPrice());
        item.setQuantity(updateParam.getQuantity());
    }

    @Override
    public Optional<Item> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        // 조건에 따라 인터페이스에 정의된 메서드를 각각 호출할 것임. (동적 쿼리 x. 그건 QueryDsl 적용 예정)
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        boolean hasItemName = StringUtils.hasText(itemName);
        boolean hasMaxPrice = (maxPrice != null);

        if(hasItemName && hasMaxPrice) {
//            return repository.findByItemNameLikeAndPriceLessThanEqual(itemName, maxPrice);
            return repository.findItems("%" + itemName + "%", maxPrice);
        }
        else if (hasItemName) {
            return repository.findByItemNameLike("%" + itemName + "%");
        }
        else if (hasMaxPrice) {
            return repository.findByPriceLessThanEqual(maxPrice);
        }
        else {
            return repository.findAll();
        }
    }

}
