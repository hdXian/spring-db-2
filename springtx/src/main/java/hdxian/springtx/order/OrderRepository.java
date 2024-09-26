package hdxian.springtx.order;

import org.springframework.data.jpa.repository.JpaRepository;

// Spring Data JPA
public interface OrderRepository extends JpaRepository<Order, Long> {

}
