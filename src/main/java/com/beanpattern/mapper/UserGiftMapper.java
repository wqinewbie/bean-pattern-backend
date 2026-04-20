package com.beanpattern.mapper;

import com.beanpattern.entity.UserGift;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户礼品 Mapper：user_gift
 */
@Mapper
public interface UserGiftMapper {

    @Select("SELECT id, user_id AS userId, gift_item_id AS giftItemId, gift_code AS giftCode, gift_name AS giftName, " +
            "gift_category AS giftCategory, value, source, task_id AS taskId, share_record_id AS shareRecordId, " +
            "order_id AS orderId, used_at AS usedAt, expire_at AS expireAt, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM user_gift WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<UserGift> findByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, gift_item_id AS giftItemId, gift_code AS giftCode, gift_name AS giftName, " +
            "gift_category AS giftCategory, value, source, task_id AS taskId, share_record_id AS shareRecordId, " +
            "order_id AS orderId, used_at AS usedAt, expire_at AS expireAt, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM user_gift WHERE user_id = #{userId} AND status = #{status} " +
            "AND (expire_at IS NULL OR expire_at > #{now}) ORDER BY created_at DESC")
    List<UserGift> findAvailableByUserId(@Param("userId") Long userId, @Param("status") Integer status, @Param("now") LocalDateTime now);

    @Insert("INSERT INTO user_gift(user_id, gift_item_id, gift_code, gift_name, gift_category, value, " +
            "source, task_id, share_record_id, order_id, expire_at, status) " +
            "VALUES(#{userId}, #{giftItemId}, #{giftCode}, #{giftName}, #{giftCategory}, #{value}, " +
            "#{source}, #{taskId}, #{shareRecordId}, #{orderId}, #{expireAt}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserGift userGift);

    @Update("UPDATE user_gift SET status = 1, used_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = #{id} AND status = 0")
    int use(@Param("id") Long id);

    @Update("UPDATE user_gift SET status = 2, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int expire(@Param("id") Long id);
    
    @Select("SELECT id, user_id AS userId, gift_item_id AS giftItemId, gift_code AS giftCode, gift_name AS giftName, " +
            "gift_category AS giftCategory, value, source, task_id AS taskId, share_record_id AS shareRecordId, " +
            "order_id AS orderId, used_at AS usedAt, expire_at AS expireAt, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM user_gift WHERE id = #{id}")
    UserGift findById(@Param("id") Long id);
}
