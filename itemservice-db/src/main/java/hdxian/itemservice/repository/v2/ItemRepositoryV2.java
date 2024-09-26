package hdxian.itemservice.repository.v2;

import hdxian.itemservice.domain.Item;
import org.springframework.data.jpa.repository.JpaRepository;

// using Spring Data JPA. for basic CRUD or select...
// only use methods what JpaRepository provides.
public interface ItemRepositoryV2 extends JpaRepository<Item, Long> {
}
