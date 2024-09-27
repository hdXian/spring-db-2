package hdxian.springtx.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public void order(Order order) throws NotEnoughMoneyException {
        log.info("order() called");
        orderRepository.save(order); // 주문은 일단 저장. 커밋 or 롤백

        String username = order.getUsername();

        log.info("결제 프로세스 진입");
        if (username.equals("정상")) {
            log.info("정상 승인");
            order.setPayStatus("완료");
        }
        else if (username.equals("예외")) {
            log.info("시스템 예외 발생");
            throw new RuntimeException("시스템 예외 발생");
        }
        else if (username.equals("잔고부족")) {
            log.info("잔고 부족 비즈니스 예외 발생");
            order.setPayStatus("대기");
            throw new NotEnoughMoneyException("잔고 부족 체크 예외");
        }
        log.info("결제 프로세스 끝");

    }

}
