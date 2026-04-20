package com.beanpattern.mapper;

import com.beanpattern.entity.GiftType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 礼品类型 Mapper：gift_type
 */
@Mapper
public interface GiftTypeMapper {

    @Select("SELECT id, code, name, gift_category AS giftCategory, description, icon_url AS iconUrl, " +
            "sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM gift_type WHERE status = 1 ORDER BY sort_order ASC")
    List<GiftType> findAllActive();

    @Select("SELECT id, code, name, gift_category AS giftCategory, description, icon_url AS iconUrl, " +
            "sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM gift_type WHERE gift_category = #{category} AND status = 1 ORDER BY sort_order ASC")
    List<GiftType> findByCategory(@Param("category") String category);

    @Select("SELECT id, code, name, gift_category AS giftCategory, description, icon_url AS iconUrl, " +
            "sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM gift_type WHERE code = #{code}")
    GiftType findByCode(@Param("code") String code);
    
    @Select("SELECT id, code, name, gift_category AS giftCategory, description, icon_url AS iconUrl, " +
            "sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM gift_type WHERE id = #{id}")
    GiftType findById(@Param("id") Long id);
}
