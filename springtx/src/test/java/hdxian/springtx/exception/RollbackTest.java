package hdxian.springtx.exception;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
public class RollbackTest {

    @Autowired
    RollbackService service;

    // runtimeEx during transaction -> rollback
    @Test
    public void runTimeEx() {
        Assertions.assertThatThrownBy(() -> service.runtimeEx())
                        .isInstanceOf(RuntimeException.class);
    }

    // checkedEx during transaction -> commit
    @Test
    public void checkedEx() {
        Assertions.assertThatThrownBy(() -> service.checkedEx())
                .isInstanceOf(MyException.class);
    }

    // checkedEx but rollbackFor -> rollback
    @Test
    public void rollbackFor() {
        Assertions.assertThatThrownBy(() -> service.rollbackFor())
                .isInstanceOf(MyException.class);
    }

    @TestConfiguration
    static class RollbackConfig {

        @Bean
        public RollbackService rollbackService() {
            return new RollbackService();
        }

    }

    @Slf4j
    static class RollbackService {

        // runtime Exception -> rollback
        @Transactional
        public void runtimeEx() {
            log.info("runtimeEx called");
            throw new RuntimeException("test re");
        }

        // checked Exception -> commit
        @Transactional
        public void checkedEx() throws MyException {
            log.info("checkedEx called");
            throw new MyException();
        }

        // rollbackFor for checked Exception -> rollback
        @Transactional(rollbackFor = MyException.class)
        public void rollbackFor() throws MyException {
            log.info("rollBackFor called");
            throw new MyException();
        }

    }

    static class MyException extends Exception {

    }

}
