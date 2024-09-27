package hdxian.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest // @Transactional -> AOP -> need to Spring container
public class TxApplyBasicTest {

    @Autowired
    BasicService service;

    // @Transactional이 메서드나 클래스에 하나라도 붙어있으면 해당 클래스는 AOP 프록시의 대상이 된다.
    @Test
    public void checkAop() {
        log.info("aop class={}", service.getClass());
         assertThat(AopUtils.isAopProxy(service)).isTrue();
    }

    // 프록시 객체는 @Transactional이 붙은 메서드에 한해 트랜잭션을 시작하고 종료한다.
    @Test
    public void txTest() {
        service.tx();
        service.nonTx();
    }

    @TestConfiguration
    static class TxApplyBasicConfig {
        @Bean
        public BasicService basicService() {
            return new BasicService();
        }
    }

    @Slf4j
    static class BasicService {

        @Transactional
        public void tx() {
            log.info("tx called");
            // 현재 스레드의 트랜잭션 적용 여부를 확인하는 코드 (트랜잭션 매니저 사용)
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }

        public void nonTx() {
            log.info("non tx called");
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }

    }


}
