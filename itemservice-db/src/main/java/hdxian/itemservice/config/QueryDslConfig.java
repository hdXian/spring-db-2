package hdxian.itemservice.config;

import hdxian.itemservice.repository.ItemRepository;
import hdxian.itemservice.repository.jpa.JpaItemRepositoryV3;
import hdxian.itemservice.service.ItemService;
import hdxian.itemservice.service.ItemServiceV1;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class QueryDslConfig {

    // DI - JpaRepoV3 내부의 QueryFactory는 EntityManager를 의존함.
    private final EntityManager em;

    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV3(em);
    }

    @Bean
    public ItemService itemService() {
        return new ItemServiceV1(itemRepository());
    }

}
