package com.beanpattern.mapper;

import com.beanpattern.entity.GiftItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 礼品项 Mapper：gift_item
 */
@Mapper
public interface GiftItemMapper {

    @Select("SELECT id, gift_type_id AS giftTypeId, gift_code AS giftCode, name, value, " +
            "total_quantity AS totalQuantity, remain_quantity AS remainQuantity, " +
            "start_at AS startAt, end_at AS endAt, sort_order AS sortOrder, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM gift_item WHERE gift_type_id = #{giftTypeId} AND status = 1 " +
            "AND (start_at IS NULL OR start_at <= #{now}) " +
            "AND (end_at IS NULL OR end_at >= #{now}) " +
            "ORDER BY sort_order ASC")
    List<GiftItem> findAvailableByTypeId(@Param("giftTypeId") Long giftTypeId, @Param("now") LocalDateTime now);

    @Select("SELECT id, gift_type_id AS giftTypeId, gift_code AS giftCode, name, value, " +
            "total_quantity AS totalQuantity, remain_quantity AS remainQuantity, " +
            "start_at AS startAt, end_at AS endAt, sort_order AS sortOrder, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM gift_item WHERE gift_code = #{giftCode}")
    GiftItem findByCode(@Param("giftCode") String giftCode);

    @Select("SELECT id, gift_type_id AS giftTypeId, gift_code AS giftCode, name, value, " +
            "total_quantity AS totalQuantity, remain_quantity AS remainQuantity, " +
            "start_at AS startAt, end_at AS endAt, sort_order AS sortOrder, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM gift_item WHERE status = 1 " +
            "AND (start_at IS NULL OR start_at <= #{now}) " +
            "AND (end_at IS NULL OR end_at >= #{now}) " +
            "ORDER BY sort_order ASC")
    List<GiftItem> findAllAvailable(@Param("now") LocalDateTime now);

    @Update("UPDATE gift_item SET remain_quantity = remain_quantity - 1 WHERE id = #{id} AND remain_quantity > 0")
    int decrementQuantity(@Param("id") Long id);

    @Update("UPDATE gift_item SET remain_quantity = remain_quantity + 1 WHERE id = #{id} AND total_quantity > 0")
    int incrementQuantity(@Param("id") Long id);

    @Select("SELECT id, gift_type_id AS giftTypeId, gift_code AS giftCode, name, value, " +
            "total_quantity AS totalQuantity, remain_quantity AS remainQuantity, " +
            "start_at AS startAt, end_at AS endAt, sort_order AS sortOrder, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM gift_item WHERE id = #{id}")
    GiftItem findById(@Param("id") Long id);
}
