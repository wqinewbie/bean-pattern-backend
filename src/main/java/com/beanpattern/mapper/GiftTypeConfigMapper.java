package com.beanpattern.mapper;

import com.beanpattern.entity.GiftTypeConfig;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GiftTypeConfigMapper {

    @Select("SELECT id, code, name, gift_category AS giftCategory, description, icon_url AS iconUrl, sort_order AS sortOrder, status, value_type AS valueType, target_product_type AS targetProductType, created_at AS createdAt, updated_at AS updatedAt FROM bp_gift_type ORDER BY sort_order ASC, id ASC")
    List<GiftTypeConfig> findAll();

    @Select("SELECT id, code, name, gift_category AS giftCategory, description, icon_url AS iconUrl, sort_order AS sortOrder, status, value_type AS valueType, target_product_type AS targetProductType, created_at AS createdAt, updated_at AS updatedAt FROM bp_gift_type WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<GiftTypeConfig> findAllActive();

    @Select("SELECT id, code, name, gift_category AS giftCategory, description, icon_url AS iconUrl, sort_order AS sortOrder, status, value_type AS valueType, target_product_type AS targetProductType, created_at AS createdAt, updated_at AS updatedAt FROM bp_gift_type WHERE id = #{id}")
    GiftTypeConfig findById(@Param("id") Long id);

    @Select("SELECT id, code, name, gift_category AS giftCategory, description, icon_url AS iconUrl, sort_order AS sortOrder, status, value_type AS valueType, target_product_type AS targetProductType, created_at AS createdAt, updated_at AS updatedAt FROM bp_gift_type WHERE code = #{code}")
    GiftTypeConfig findByCode(@Param("code") String code);

    @Insert("INSERT INTO bp_gift_type(code, name, gift_category, description, icon_url, sort_order, status, value_type, target_product_type) VALUES(#{code}, #{name}, #{giftCategory}, #{description}, #{iconUrl}, #{sortOrder}, #{status}, #{valueType}, #{targetProductType})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GiftTypeConfig giftTypeConfig);

    @Update("UPDATE bp_gift_type SET code=#{code}, name=#{name}, gift_category=#{giftCategory}, description=#{description}, icon_url=#{iconUrl}, sort_order=#{sortOrder}, status=#{status}, value_type=#{valueType}, target_product_type=#{targetProductType}, updated_at=NOW() WHERE id=#{id}")
    int update(GiftTypeConfig giftTypeConfig);

    @Update("UPDATE bp_gift_type SET status = CASE WHEN status=1 THEN 0 ELSE 1 END, updated_at=NOW() WHERE id=#{id}")
    int toggleStatus(@Param("id") Long id);
}
