package hdxian.springtx.propagation;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final EntityManager em;

    @Transactional // JPA의 모든 데이터 변경은 트랜잭션 내에서 이루어져야 한다.
    public void save(Member member) {
        em.persist(member);
        log.info("save member={}", member);
    }

    public Optional<Member> find(String name) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", name)
                .getResultList()
                .stream().findAny();
    }

}
