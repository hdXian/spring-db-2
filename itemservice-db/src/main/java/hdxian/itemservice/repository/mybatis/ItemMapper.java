package hdxian.itemservice.repository.mybatis;

import hdxian.itemservice.domain.Item;
import hdxian.itemservice.repository.ItemSearchCond;
import hdxian.itemservice.repository.ItemUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ItemMapper {

    // 메서드 파라미터가 하나일 경우 @Param 생략 가능.
    // able to omit @Param if there are only one parameter on method.

    void save(Item item);

    void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);

    Optional<Item> findById(Long id);

    List<Item> findAll(ItemSearchCond cond);

}
