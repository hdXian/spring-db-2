package hdxian.springtx.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class TxLevelTest {

    @Autowired
    LevelService service;

    @Test
    public void orderTest() {
        service.write();
        service.read();
    }

    @TestConfiguration
    static class LevelTestConfig {
        @Bean
        public LevelService levelService() {
            return new LevelService();
        }
    }

    // @Transactional의 옵션 적용은 더 자세한 것이 우선 순위를 가진다. (전체 스프링 설정에서 통용되는 방식)
    @Transactional(readOnly = true)
    static class LevelService {

        // readOnly false 적용 (메서드 레벨의 @Transactional 옵션이 적용됨)
        @Transactional(readOnly = false) // readOnly=false는 기본 옵션으로 생략 가능 -> @Transactional만 적어도 false 설정을 개별 적용 가능함.
        public void write() {
            log.info("write called");
            printTxInfo();
        }

        // readOnly true 적용 (클래스 레벨의 @Transactional 옵션이 적용됨)
        public void read() {
            log.info("read called");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean isTxActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("isTxActive={}", isTxActive);
            boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            log.info("isReadOnly={}", isReadOnly);
        }

    }

}
