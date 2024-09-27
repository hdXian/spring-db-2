package hdxian.springtx.apply;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class TxInitTest {

    @Autowired
    Hello hello;

    @Test
    public void t1() {
//        hello.initV1(); // @PostConstruct 메서드는 객체 초기화 시점에 스프링이 자동 호출
    }

    @TestConfiguration
    static class TxInitTestConfig {
        @Bean
        Hello hello() {
            return new Hello();
        }
    }


    @Slf4j
    static class Hello {

        // PostConstruct 시점에는 Transactional이 적용되지 않는다. 트랜잭션 AOP 생성 및 적용 시점이 PostConstruct 이후이기 때문.
        @PostConstruct
        @Transactional
        public void initV1() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello.init @PostConstruct txActive = {}", txActive);
        }

        // ApplicationReadyEvent는 AOP를 포함한 스프링 컨테이너의 모든 구성이 완료되었을 때 발생하는 이벤트다.
        // 이 시점에서 트랜잭션을 적용할 메서드를 호출하는 경우 트랜잭션 AOP가 적용된다.
        @EventListener(value = ApplicationReadyEvent.class)
        @Transactional
        public void initV2() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello.init ApplicationReadyEvent txActive = {}", txActive);
        }

    }
}
