package hdxian.springtx.propagation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter
@ToString
public class Member {

    @Id @GeneratedValue
    private Long id;
    private String username;

    // JPA 스펙상 기본 생성자가 필요
    public Member() {
    }

    public Member(String username) {
        this.username = username;
    }

}
