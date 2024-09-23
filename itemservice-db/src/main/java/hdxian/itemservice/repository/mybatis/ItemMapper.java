package hdxian.itemservice.repository.mybatis;

import hdxian.itemservice.domain.Item;
import hdxian.itemservice.repository.ItemSearchCond;
import hdxian.itemservice.repository.ItemUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ItemMapper {

    // 메서드 파라미터가 하나일 경우 @Param 생략 가능.
    // able to omit @Param if there are only one parameter on method.

    void save(Item item);

    void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);

//    @Select("select item_name, price, quantity from item where id = #{id}") // 이거 쓰려면 xml에는 해당 부분이 없어야 함 (같이 있으면 충돌남)
    Optional<Item> findById(Long id);

    List<Item> findAll(ItemSearchCond cond);

}
