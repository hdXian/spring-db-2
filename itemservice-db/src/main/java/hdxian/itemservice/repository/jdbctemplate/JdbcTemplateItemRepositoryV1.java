package hdxian.itemservice.repository.jdbctemplate;

import hdxian.itemservice.domain.Item;
import hdxian.itemservice.repository.ItemRepository;
import hdxian.itemservice.repository.ItemSearchCond;
import hdxian.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {

    private final JdbcTemplate template;

    public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
        // 관례적인 JdbcTemplate 초기화 방식 (그냥 JdbcTemplate를 빈으로 주입받아도 됨)
        this.template = new JdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        String sql = "insert into item (item_name, price, quantity) values (?, ?, ?)";

        // DB 내부에 자동 생성된 ID를 가져오는 로직 (리턴할 item 객체에 설정해줘야 함)
        KeyHolder keyHolder = new GeneratedKeyHolder();
        template.update(connection -> {
            // 템플릿 내 자동 생성되는 connection을 통해 psmt를 설정하는 부분을 직접 해줘야 하는듯.
            // 이걸 실행하고 KeyHolder를 통해 id값을 가져오나 봄.
            PreparedStatement psmt = connection.prepareStatement(sql, new String[]{"id"});
            psmt.setString(1, item.getItemName());
            psmt.setInt(2, item.getPrice());
            psmt.setInt(3, item.getQuantity());
            return psmt;
        }, keyHolder);

        // keyHolder로 뽑아온 id를 설정.
        long id = keyHolder.getKey().longValue();
        item.setId(id);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name=?, price=?, quantity=? where id=?";
        // sql 이후 지정한 순서대로 파라미터 지정
        template.update(sql, updateParam.getItemName(), updateParam.getPrice(), updateParam.getQuantity(), itemId);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id=?";

        try {
            Item item = template.queryForObject(sql, itemRowMapper(), id); // 결과가 null이면 (못 찾으면) Ex 뱉음.
            return Optional.of(item); // Optional.of()는 null을 허용하지 않음.
        } catch (EmptyResultDataAccessException e) {
            // queryForObject()는 조회 결과가 없으면 EmptyResultDataAccessException를 뱉음.
            log.error("[JdbcTemplateItemRepositoryV1.findById] failed to find Item (Empty Result)");
            return Optional.empty();
        }

    }

    // 조건에 따라 Item을 검색한 조건을 List로 리턴
    @Override
    public List<Item> findAll(ItemSearchCond cond) {
        String itemName = cond.getItemName();
        Integer maxPrice = cond.getMaxPrice();

        String sql = "select id, item_name, price, quantity from item";

        // 동적 쿼리 생성
        if(StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;
        List<Object> param = new ArrayList<>();

        // 이름 검색 조건 확인
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%', ?, '%')";
            param.add(itemName);
            andFlag = true;
        }

        // 최대 가격 검색 조건 확인
        if (maxPrice != null) {
            if (andFlag)
                sql += " and";

            sql += " price <= ?";
            param.add(maxPrice);
        }

        log.info("[JdbcTemplateItemRepositoryV1.findAll] generated sql={}", sql);
        // template.query() - 여러 줄의 결과를 받을 때 사용. 리턴 타입은 아마 RowMapper 보고 만들어지는 걸꺼임 (<T> 등으로). 리턴 타입 확인을 위해 일부러 축약 안했음.
        List<Item> result = template.query(sql, itemRowMapper(), param.toArray()); // 마지막 인자가 Object... args임 -> Object 여러개나 배열로 받을 수 있음.
        return result;
    }

    private RowMapper<Item> itemRowMapper() {
        // () -> {} 로 지정한 함수를 리턴한다는 개념.
        // 템플릿이 DB 실행 결과로 들어오는 ResultSet에 대해 이 함수를 각각 실행해서 결과를 리탄해주는 듯.
        return (rs, rowNum) -> {
            Item item = new Item();
            item.setId(rs.getLong("id"));
            item.setItemName(rs.getString("item_name"));
            item.setPrice(rs.getInt("price"));
            item.setQuantity(rs.getInt("quantity"));
            return item;
        };
    }

}
