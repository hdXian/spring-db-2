package hdxian.springtx.propagation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter
@ToString
public class Log {

    @Id @GeneratedValue @Column(name = "id")
    private Long logId;
    private String message; // 로그에 저장할 사용자명

    // JPA 스펙상 기본 생성자가 필요
    public Log() {
    }

    public Log(String message) {
        this.message = message;
    }

}
