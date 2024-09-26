package hdxian.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;


@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    // 정상 로직 -> 커밋
    @Test
    void complete() throws NotEnoughMoneyException {
        // given
        Order order = new Order();
        order.setUsername("정상");

        // when
        orderService.order(order); // 내부에서 repo.save()를 통해 id값이 order에 이미 들어옴

        // then
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("완료");
    }

    // 런타임 예외 -> 롤백
    @Test
    void runtimeEx() throws NotEnoughMoneyException {
        // given
        Order order = new Order();
        order.setUsername("예외");

        // when
        assertThatThrownBy(() -> orderService.order(order)).isInstanceOf(RuntimeException.class);

        // then: 롤백 됐으므로 findById로 찾은게 없어야 함.
        // service 로직 내의 repo.save()에 의해 데이터가 저장되고, order 객체의 id값에 1이 저장됨. 하지만 예외가 터져서 롤백됐기 때문에 DB에는 반영되지 않고,
        // order 객체에 세팅된 id값 1만 남게됨.
//        System.out.println("order.getId() = " + order.getId()); // 1
        Optional<Order> orderOptional = orderRepository.findById(order.getId());
        assertThat(orderOptional.isEmpty()).isTrue();

    }

    // 체크 예외 -> 커밋
    @Test
    void bizEx() {
        // given
        Order order = new Order();
        order.setUsername("잔고부족");


        // when
        try {
            orderService.order(order);
            fail("잔고 부족 예외가 발생해야 합니다."); // 테스트를 실패로 처리하는 메서드 fail()
        } catch (NotEnoughMoneyException e) {
            log.info("고객에게 잔고 부족을 알리고 별도의 계좌로 입금하도록 안내 (비즈니스 예외 처리 로직)");
        }

        // then: status가 "대기"로 설정된 채로 커밋되어 있어야 함.
        Order findOrder = orderRepository.findById(order.getId()).get();
        assertThat(findOrder.getPayStatus()).isEqualTo("대기");
    }

}