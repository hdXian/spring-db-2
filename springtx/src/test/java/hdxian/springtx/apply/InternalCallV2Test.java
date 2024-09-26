package hdxian.springtx.apply;

import lombok.RequiredArgsConstructor;
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

// V2 - Separate Transactional method to another class

@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired
    CallService service;

    @Test
    public void checkAop() {
        // CallService isn't AOP Proxy anymore.
        log.info("service class={}", service.getClass());
//        Assertions.assertThat(AopUtils.isAopProxy(service)).isTrue();
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
            return new CallService(internalService());
        }

        @Bean
        public InternalService internalService() {
            return new InternalService();
        }

    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external() {
            log.info("external called");
            printTxInfo();
            internalService.internal();
        }

        public void internal() {
            internalService.internal();
        }

        private void printTxInfo() {
            boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("isTxActive={}", isTxActive);
        }

    }


    @Slf4j
    static class InternalService {

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
