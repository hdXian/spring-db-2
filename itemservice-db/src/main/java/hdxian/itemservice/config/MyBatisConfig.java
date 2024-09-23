package hdxian.itemservice.config;

import hdxian.itemservice.repository.ItemRepository;
import hdxian.itemservice.repository.mybatis.ItemMapper;
import hdxian.itemservice.repository.mybatis.MybatisItemRepository;
import hdxian.itemservice.service.ItemService;
import hdxian.itemservice.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MyBatisConfig {

    private final ItemMapper itemMapper;

    @Bean
    public ItemRepository itemRepository() {
        return new MybatisItemRepository(itemMapper);
    }

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

}
