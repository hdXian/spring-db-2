package hdxian.springtx.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final LogRepository logRepository;

    @Transactional
    public void joinV1(String username) {
        Member member = new Member(username);
        Log terms = new Log(username); // 그냥 username 값을 message로 가지는 Log 생성

        log.info("=== memberRepository 호출 시작 ===");
        memberRepository.save(member);
        log.info("=== memberRepository 호출 종료 ===");

        log.info("=== logRepository 호출 시작 ===");
        logRepository.save(terms);
        log.info("=== logRepository 호출 종료 ===");
    }

    @Transactional
    public void joinV2(String username) {
        Member member = new Member(username);
        Log terms = new Log(username); // 그냥 username 값을 message로 가지는 Log 생성

        log.info("=== V2 - memberRepository 호출 시작 ===");
        memberRepository.save(member);
        log.info("=== V2 - memberRepository 호출 종료 ===");

        log.info("=== V2 - logRepository 호출 시작 ===");
        try {
            logRepository.save(terms);
        } catch (RuntimeException e) {
            log.info("log 저장 실패");
            log.info("정상 흐름 반환");
        }
        log.info("=== V2 - logRepository 호출 종료 ===");
    }

}
