package hdxian.itemservice;

import hdxian.itemservice.config.*;
import hdxian.itemservice.repository.ItemRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

//@Import(MemoryConfig.class)
//@Import(JdbcTemplateV1Config.class)
//@Import(JdbcTemplateV2Config.class)
//@Import(JdbcTemplateV3Config.class)
//@Import(MyBatisConfig.class)
//@Import(JpaConfig.class)
@Import(SpringDataJpaConfig.class)
@SpringBootApplication(scanBasePackages = "hdxian.itemservice.web")
public class ItemServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemServiceApplication.class, args);
	}

	@Bean
	@Profile("local")
	public TestDataInit testDataInit(ItemRepository itemRepository) {
		return new TestDataInit(itemRepository);
	}

	// 테스트에서 사용할 메모리 DB에 대한 DataSource 빈을 수동 등록
//	@Bean
//	@Profile("test") // 프로필이 test인 스프링 컨테이너에서는 이 빈이 등록되어 사용될 것.
//	public DataSource dataSource() {
//		DriverManagerDataSource dataSource = new DriverManagerDataSource();
//		dataSource.setDriverClassName("org.h2.Driver");
//		dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
//		dataSource.setUsername("sa");
//		dataSource.setPassword("");
//		return dataSource;
//	}
	// 메모리 DB (임베디드 DB)는 어플리케이션 생성 시점에 DB가 생성되므로, 어플리케이션을 실행할 때마다 테이블을 만들어줘야 함.
	// -> resources에 schema.sql을 등록하면 스프링 부트가 어플리케이션 생성 시점에 임베디드DB에 해당 sql을 실행해줌.
	// +) 스프링 부트는 DB 설정이 없으면 자동으로 임베디드 DB를 사용하는 DataSource를 등록해 사용함.

}
