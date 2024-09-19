package hdxian.itemservice.config;

import hdxian.itemservice.repository.ItemRepository;
import hdxian.itemservice.repository.memory.MemoryItemRepository;
import hdxian.itemservice.service.ItemService;
import hdxian.itemservice.service.ItemServiceV1;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryConfig {

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

    @Bean
    public ItemRepository itemRepository() {
        return new MemoryItemRepository();
    }

}
