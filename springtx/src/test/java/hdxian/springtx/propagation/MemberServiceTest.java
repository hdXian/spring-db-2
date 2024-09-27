package hdxian.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    LogRepository logRepository;

    /**
     * MemberService     @Transactional OFF
     * MemberRepository  @Transactional ON
     * LogRepository     @Transactional ON
     */
    @Test
    void outerTxOff_success() {
        // given
        String username = "outerTxOff_success";

        // when
        memberService.joinV1(username);

        // then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());

    }

    /**
     * MemberService     @Transactional OFF
     * MemberRepository  @Transactional ON
     * LogRepository     @Transactional ON (Exception occurs)
     */
    @Test
    void outerTxOff_fail() {
        // given
        String username = "로그예외outerTxOff_success"; // message.contains("로그예외") -> 예외 터지도록 logRepo에 작성해놓음.

        // when
        Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // then
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());

    }

    /**
     * MemberService     @Transactional ON
     * MemberRepository  @Transactional OFF
     * LogRepository     @Transactional OFF
     */
    @Test
    void singleTx() {
        // MemberService.joinV1()에는 @Transactional을 걸고, repo들의 save()에는 @Transactional을 주석 처리한 다음 진행.
        // given
        String username = "singleTx";

        // when
        memberService.joinV1(username);

        // then -> 모든 코드가 하나의 트랜잭션 안에서 수행되므로 정상 동작.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());

    }

    /**
     * MemberService     @Transactional ON
     * MemberRepository  @Transactional ON
     * LogRepository     @Transactional ON
     */
    @Test
    void outerTxOn_success() {
        // service, repo에 모두 @Transactional을 걸고 트랜잭션 전파
        // given
        String username = "outerTxOn_success";

        // when
        memberService.joinV1(username);

        // then -> 전파된 다른 트랜잭션들도 정상 종료되어 정상 동작.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());

    }

    /**
     * MemberService     @Transactional ON
     * MemberRepository  @Transactional ON
     * LogRepository     @Transactional ON (Exception occurs)
     */
    @Test
    void outerTxOn_fail() {
        // given
        String username = "로그예외outerTxOn_fail"; // message.contains("로그예외") -> 예외 터지도록 logRepo에 작성해놓음.

        // when
        Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // then
        // 트랜잭션을 시작한 service까지 예외가 올라왔기 때문에, 물리적으로 롤백 요청이 날아감.
        // 전체 데이터가 롤백된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * MemberService     @Transactional ON
     * MemberRepository  @Transactional ON
     * LogRepository     @Transactional ON (Exception occurs)
     */
    // 내부 트랜잭션에서 올라온 예외를 try-catch로 잡으면 정상 흐름으로 돌아오면서 커밋되지 않을까?
    // X. 내부 트랜잭션에서 예외가 터지고, 내부 트랜잭션에서 롤백이 요청된 순간 현재 물리 트랜잭션의 rollbackOnly가 true로 설정된다.
    // 최종적으로 해당 트랜잭션은 전체 롤백된다.
    @Test
    void recoverEx_fail() {
        // 논리 트랜잭션 중 어느 하나라도 실패하면 전체 트랜잭션이 롤백된다는 점을 기억할 것. (required 전파 옵션을 전제)

        // given
        String username = "로그예외recoverEx_fail"; // message.contains("로그예외") -> 예외 터지도록 logRepo에 작성해놓음.

        // when
        // joinV2() -> try-catch로 올라온 런타임 예외를 처리하는 로직이 있음.
        Assertions.assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class); // -> 예외 뭐가 터지는지 확인

        // log: On commit, transaction was marked for roll-back only, rolling back...

        // then -> 최종적으로 해당 트랜잭션은 전체 롤백된다.
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * MemberService     @Transactional ON
     * MemberRepository  @Transactional ON
     * LogRepository     @Transactional ON (requires_new) (Exception occurs)
     */
    // logRepo.save()에 @Transactional(propagation = Propagation.REQUIRES_NEW) 추가
    @Test
    void recoverEx_success() {
        // given
        String username = "로그예외recoverEx_success"; // message.contains("로그예외") -> 예외 터지도록 logRepo에 작성해놓음.

        /** when에서의 흐름
         * 1. logRepo.save()에서 예외가 터진다.
         * 2. logRepo.save()의 전파 옵션은 requires_new이므로 별개의 트랜잭션에서 동작한다.
         * 3. 예외가 발생헀으므로 logRepo.save()의 트랜잭션에서 물리적으로 롤백이 호출된다.
         * 4. 예외는 service까지 계속해서 넘어오는데, joinV2()에서 예외를 처리했기 때문에 정상 흐름으로 넘어간다.
         * 5. service까지 최종적으로 정상 흐름이고, rollbackOnly도 설정되지 않았으므로 물리적으로 커밋이 호출된다.
         * 6. 결과적으로 커밋으로 처리된 member는 DB에 저장되고, 롤백된 log는 DB에 저장되지 않는다.
         */
        // when
        memberService.joinV2(username); // UnexpectedRollbackException이 발생하지 않는다.

        // log: On commit, transaction was marked for roll-back only, rolling back...

        // then -> member는 커밋되고, log는 롤백된다.
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }


}