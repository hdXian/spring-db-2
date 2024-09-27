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

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired
    CallService service;

    @Test
    public void checkAop() {
        log.info("service class={}", service.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(service)).isTrue();
    }

    // 프록시 객체를 참조하는 service를 통해 internal()을 호출하면 프록시 객체에 의해 트랜잭션이 적용된다.
    @Test
    public void internalCall() {
        service.internal();
    }

    // external()을 통해 내부적으로 internal()을 호출하면 this.internal() 의 방식으로 호출되므로 프록시 객체를 거치지 않는다.
    // 즉 프록시 객체를 거치지 않고 호출되므로 @Transactional이 붙어 있어도 internal()에 트랜잭션이 적용되지 않는다. (AOP 프록시의 한계 - 내부 호출에 프록시를 적용할 수 없음.)
    @Test
    public void externalCall() {
        service.external();
    }

    @TestConfiguration
    static class CallConfig {
        @Bean
        public CallService callService() {
            return new CallService();
        }
    }

    @Slf4j
    static class CallService {

        public void external() {
            log.info("external called");
            printTxInfo();
            internal();
        }

        @Transactional
        public void internal() {
            log.info("internal called");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("isTxActive={}", isTxActive);
        }

    }

}
