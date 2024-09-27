package hdxian.springtx.propagation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LogRepository {

    private final EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW) // JPA의 모든 데이터 변경은 트랜잭션 안에서 이루어져야 함.
    public void save(Log logMsg) {
        em.persist(logMsg);
        log.info("save log={}", log);

        // 예외 로직 추가 (트랜잭션 동작 확인 용도)
        if (logMsg.getMessage().contains("로그예외")) {
            log.info("log 저장 중 예외 발생");
            throw new RuntimeException("로그 예외 발생");
        }

    }

    public Optional<Log> find(String message) {
        return em.createQuery("select l from Log l where l.message = :message", Log.class)
                .setParameter("message", message)
                .getResultList().stream().findAny();
    }

}
