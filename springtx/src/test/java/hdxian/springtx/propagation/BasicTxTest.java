package hdxian.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    @RequiredArgsConstructor
    static class testConfig {

        private final DataSource dataSource;

        @Bean
        public PlatformTransactionManager transactionManager() {
            // TxManager of JDBC
            return new DataSourceTransactionManager(dataSource);
        }

    }

    @Test
    void commit() {
        /**
         * 복습
         * @Transactional이 적용된 AOP 프록시의 역할
         * txManager.getTransaction()
         * txManager.commit() or txManager.rollback();
         * 등의 트랜잭션 작업을 대신 처리해주는 것.
         */
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void commit_twice() {
        log.info("트랜잭션1 시작");
        TransactionStatus txStatus1 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션1 커밋");
        txManager.commit(txStatus1);

        log.info("트랜잭션2 시작");
        TransactionStatus txStatus2 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션2 커밋");
        txManager.commit(txStatus2);
    }

    @Test
    void commit_rollback_each() {
        log.info("트랜잭션1 시작");
        TransactionStatus txStatus1 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션1 커밋");
        txManager.commit(txStatus1);

        log.info("트랜잭션2 시작");
        TransactionStatus txStatus2 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션2 롤백");
        txManager.rollback(txStatus2);
    }

    @Test
    void inner_commit() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("외부 트랜잭션 isNewTx={}", outerStatus.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        // 이 시점에 participating existing transaction 로그 발생
        TransactionStatus innerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 isNewTx={}", innerStatus.isNewTransaction());

        log.info("내부 트랜잭션 커밋");
        txManager.commit(innerStatus);
        // 이 시점에는 실제로 커밋되지 않음 (확인해보면 로그도 없음)

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outerStatus);
        // 이 시점에 실제로 커밋됨 -> 실제 커밋하는 기준: 새로 시작한 트랜잭션인가 (isNewTransaction)
    }

    // 참여한 논리 트랜잭션 중 하나라도 롤백할 경우 전체 트랜잭션은 롤백된다.
    // 1. 외부 트랜잭션이 롤백한 경우
    @Test
    void outer_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("외부 트랜잭션 isNewTx={}", outerStatus.isNewTransaction());
        // ex) insert A ...

        log.info("내부 트랜잭션 시작");
        TransactionStatus innerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 isNewTx={}", innerStatus.isNewTransaction());
        // ex) insert B ...

        log.info("내부 트랜잭션 커밋");
        txManager.commit(innerStatus);

        log.info("외부 트랜잭션 롤백");
        txManager.rollback(outerStatus);
        // insert A -> rollback
        // insert B -> rollback
    }

    // 참여한 논리 트랜잭션 중 하나라도 롤백할 경우 전체 트랜잭션은 롤백된다.
    // 2. 내부 트랜잭션이 롤백한 경우
    // 내부 트랜잭션은 롤백이 발생해도 실제 롤백을 호출할 수 없다.
    // -> 대신 트랜잭션 동기화 매니저를 통해 현재 물리 트랜잭션을 rollback only로 설정한다.
    // -> 외부 트랜잭션에서 커밋을 호출해도 전체 트랜잭션이 rollback only로 설정되어 있어 최종적으로 롤백된다.
    // -> 이 경우 외부 트랜잭션 입장에서는 커밋을 호출했음에도 롤백됐으므로 예외를 터뜨려 알려준다.
    @Test
    void inner_rollback() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("외부 트랜잭션 isNewTx={}", outerStatus.isNewTransaction());
        // ex) insert A ...

        log.info("내부 트랜잭션 시작");
        TransactionStatus innerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 isNewTx={}", innerStatus.isNewTransaction());
        // ex) insert B ...

        log.info("내부 트랜잭션 롤백");
        txManager.rollback(innerStatus);

        log.info("외부 트랜잭션 커밋");
        Assertions.assertThatThrownBy(() -> txManager.commit(outerStatus))
                .isInstanceOf(UnexpectedRollbackException.class);
        // insert A -> rollback
        // insert B -> rollback
    }

    // requires_new 옵션을 사용해 내부 트랜잭션을 별개의 물리 트랜잭션으로 분리
    @Test
    void inner_rollback_requires_new() {
        log.info("외부 트랜잭션 시작");
        TransactionStatus outerStatus = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("외부 트랜잭션 isNew={}", outerStatus.isNewTransaction());
        // insert A...

        // TransactionDefinition을 통해 requires_new 설정
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // 트랜잭션 전파 옵션을 requires_new로 지정
        // requires-new는 트랜잭션을 새로 시작하기 때문에 DB 커넥션을 하나 더 가져온다. -> 하나의 로직에 여러 DB 커넥션이 붙게 되므로 주의

        log.info("내부 트랜잭션 시작, 근데 requires_new를 곁들인");
        // log: Suspending current transaction...
        TransactionStatus innerStatus = txManager.getTransaction(definition);
        log.info("내부 트랜잭션 isNew={}", outerStatus.isNewTransaction());
        // insert B...

        // 주의) 내부 트랜잭션이 실행되는 동안 외부 트랜잭션이 일시중지되기 때문에, 트랜잭션 종료 순서는 지켜야 함
        log.info("내부 트랜잭션 롤백");
        txManager.rollback(innerStatus);
        // insert A -> rollback
        // log: Resuming suspended transaction...

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outerStatus);
        // insert B -> commit
    }

}
