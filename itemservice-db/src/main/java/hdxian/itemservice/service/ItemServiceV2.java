package hdxian.itemservice.service;

import hdxian.itemservice.domain.Item;
import hdxian.itemservice.repository.ItemSearchCond;
import hdxian.itemservice.repository.ItemUpdateDto;
import hdxian.itemservice.repository.v2.ItemQueryRepositoryV2;
import hdxian.itemservice.repository.v2.ItemRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional // in this logic, transaction starts at here(service).
public class ItemServiceV2 implements ItemService{

    private final ItemRepositoryV2 itemRepositoryV2; // Spring Data JPA (for basic CRUD)
    private final ItemQueryRepositoryV2 queryRepositoryV2; // QueryDsl (for complex query)

    @Override
    public Item save(Item item) {
        return itemRepositoryV2.save(item); // spring data jpa
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = itemRepositoryV2.findById(itemId).orElseThrow(); // spring data jpa
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
        // update query executed at end of transaction
    }

    @Override
    public Optional<Item> findById(Long id) {
        return itemRepositoryV2.findById(id); // spring data jpa
    }

    @Override
    public List<Item> findItems(ItemSearchCond cond) {
        return queryRepositoryV2.findAll(cond); // QueryDsl
    }
}
