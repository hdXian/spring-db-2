package hdxian.itemservice.config;

import hdxian.itemservice.repository.ItemRepository;
import hdxian.itemservice.repository.jpa.JpaItemRepositoryV3;
import hdxian.itemservice.repository.v2.ItemQueryRepositoryV2;
import hdxian.itemservice.repository.v2.ItemRepositoryV2;
import hdxian.itemservice.service.ItemServiceV2;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class V2Config {

    // ServiceV2는 ItemRepository 인터페이스에 의존하지 않고, ItemRepositoryV2(Spring Data JPA)와 ItemQueryRepositoryV2(QueryDsl)를 각각 직접 의존함.

    private final EntityManager em;
    private final ItemRepositoryV2 repositoryV2; // Spring Data JPA is automatically registered as bean by spring boot.

    @Bean
    public ItemServiceV2 itemServiceV2() {
        return new ItemServiceV2(repositoryV2, queryRepositoryV2());
    }

    @Bean
    public ItemQueryRepositoryV2 queryRepositoryV2() {
        return new ItemQueryRepositoryV2(em);
    }

    // for test code (테스트코드는 여전히 ItemRepository 인터페이스에 의존함. 예제에서 이 케이스에 대한 테스트 코드를 작성하지 않았음.)
    @Bean
    public ItemRepository itemRepository() {
        return new JpaItemRepositoryV3(em);
    }

}
