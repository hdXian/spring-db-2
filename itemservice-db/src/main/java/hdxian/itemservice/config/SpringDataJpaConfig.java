package hdxian.itemservice.config;

import hdxian.itemservice.repository.ItemRepository;
import hdxian.itemservice.repository.jpa.JpaItemRepositoryV2;
import hdxian.itemservice.repository.jpa.SpringDataJpaItemRepository;
import hdxian.itemservice.service.ItemService;
import hdxian.itemservice.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SpringDataJpaConfig {

    private final SpringDataJpaItemRepository springDataJpaItemRepository;

    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV2(springDataJpaItemRepository);
    }

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

}
