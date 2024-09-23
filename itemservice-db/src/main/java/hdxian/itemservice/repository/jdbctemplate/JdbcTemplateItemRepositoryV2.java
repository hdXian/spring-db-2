package hdxian.itemservice.repository.jdbctemplate;

import hdxian.itemservice.domain.Item;
import hdxian.itemservice.repository.ItemRepository;
import hdxian.itemservice.repository.ItemSearchCond;
import hdxian.itemservice.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * V2 - apply NamedParameterJdbcTemplate
 * sql에 파라미터를 이름으로 지정할 수 있음.
 *
 * SqlParameterSource
 * - BeanPropertySqlParameterSource
 * - MapSqlParameterSource
 *
 * java Map
 *
 * BeanPropertyRowMapper
 *
 */

@Slf4j
@Repository
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {

    private final NamedParameterJdbcTemplate template;

    public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public Item save(Item item) {
        // 파라미터에 이름을 지정해 바인딩
        String sql = "insert into item (item_name, price, quantity) values (:itemName, :price, :quantity)";

        // BeanPropertySqlParameterSource를 이용한 파라미터 바인딩 (item 객체의 프로퍼티들을 기준으로 파라미터 바인딩)
        SqlParameterSource param = new BeanPropertySqlParameterSource(item);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // update(sql, SqlParameterSource, KeyHolder) -> DB가 자동으로 생성하는 key를 간편하게 가져올 수 있음. (GeneratedKeyHolder)
        template.update(sql, param, keyHolder);

        // keyHolder로 뽑아온 id를 설정.
        long id = keyHolder.getKey().longValue();
        item.setId(id);
        return item;
    }

    @Override
    public void update(Long itemId, ItemUpdateDto updateParam) {
        String sql = "update item set item_name= :itemName, price= :price, quantity= :quantity where id= :id";

        // MapSqlParameterSource를 이용한 파라미터 바인딩 (파라미터 이름과 지정할 값을 직접 매핑)
        SqlParameterSource param = new MapSqlParameterSource()
                .addValue("id", itemId)
                .addValue("itemName", updateParam.getItemName())
                .addValue("price", updateParam.getPrice())
                .addValue("quantity", updateParam.getQuantity());

        // update(sql, SqlParameterSource)
        template.update(sql, param);
    }

    @Override
    public Optional<Item> findById(Long id) {
        String sql = "select id, item_name, price, quantity from item where id= :id";

        // 순수 자바 Map을 이용한 파라미터 바인딩
        Map<String, Long> param = Map.of("id", id);

        try {
            // queryForObject(sql, Map, RowMapper())
            Item item = template.queryForObject(sql, param, itemRowMapper()); // 결과가 null이면 (못 찾으면) Ex 뱉음.
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
        SqlParameterSource param = new BeanPropertySqlParameterSource(cond);
        // cond는 itemName, maxPrice라는 프로퍼티를 갖고 있음.

        // 동적 쿼리 생성
        if(StringUtils.hasText(itemName) || maxPrice != null) {
            sql += " where";
        }

        boolean andFlag = false;

        // 이름 검색 조건 확인
        if (StringUtils.hasText(itemName)) {
            sql += " item_name like concat('%', :itemName, '%')";
            andFlag = true;
        }

        // 최대 가격 검색 조건 확인
        if (maxPrice != null) {
            if (andFlag)
                sql += " and";

            sql += " price <= :maxPrice";
        }

        log.info("[JdbcTemplateItemRepositoryV1.findAll] generated sql={}", sql);
        // query(sql, SqlParameterSource, RowMapper())
        List<Item> result = template.query(sql, param, itemRowMapper());
        return result;
    }

    private RowMapper<Item> itemRowMapper() {
        // Item 클래스의 프로퍼티들을 바탕으로 RowMapper를 만들어줌. (RowMapper -> 쿼리 결과(ResultSet)와 객체(Item)를 바인딩)
        BeanPropertyRowMapper<Item> rowMapper = BeanPropertyRowMapper.newInstance(Item.class);
        return rowMapper;
    }

}
