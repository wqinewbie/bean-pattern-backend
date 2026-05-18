package com.beanpattern.mapper;

import com.beanpattern.entity.UserVipRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户VIP记录 Mapper：user_vip_record
 */
@Mapper
public interface UserVipRecordMapper {

    @Select("SELECT id, user_id AS userId, product_id AS productId, product_code AS productCode, " +
            "vip_level AS vipLevel, order_id AS orderId, order_no AS orderNo, " +
            "start_at AS startAt, expire_at AS expireAt, ai_used_count AS aiUsedCount, " +
            "ai_reset_at AS aiResetAt, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_vip_record WHERE user_id = #{userId} AND status = 1 " +
            "AND expire_at > #{now} ORDER BY expire_at DESC LIMIT 1")
    UserVipRecord findActiveByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Select("SELECT id, user_id AS userId, product_id AS productId, product_code AS productCode, " +
            "vip_level AS vipLevel, order_id AS orderId, order_no AS orderNo, " +
            "start_at AS startAt, expire_at AS expireAt, ai_used_count AS aiUsedCount, " +
            "ai_reset_at AS aiResetAt, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_vip_record WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<UserVipRecord> findByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO bp_user_vip_record(user_id, product_id, product_code, vip_level, order_id, order_no, " +
            "start_at, expire_at, ai_used_count, ai_reset_at, status) " +
            "VALUES(#{userId}, #{productId}, #{productCode}, #{vipLevel}, #{orderId}, #{orderNo}, " +
            "#{startAt}, #{expireAt}, 0, #{startAt}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserVipRecord record);

    @Update("UPDATE bp_user_vip_record SET ai_used_count = ai_used_count + 1 WHERE id = #{id}")
    int incrementAiUsedCount(@Param("id") Long id);

    @Update("UPDATE bp_user_vip_record SET ai_used_count = ai_used_count + 1 WHERE id = #{id} AND ai_used_count < #{quota}")
    int incrementAiUsedCountIfQuotaAvailable(@Param("id") Long id, @Param("quota") int quota);

    @Update("UPDATE bp_user_vip_record SET status = 0, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId} AND status = 1")
    int deactivateAll(@Param("userId") Long userId);

    @Update("UPDATE bp_user_vip_record SET ai_used_count = 0, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int resetAiUsedCount(@Param("id") Long id);

    @Update("UPDATE bp_user_vip_record SET ai_reset_at = #{resetAt}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateAiResetAt(@Param("id") Long id, @Param("resetAt") LocalDateTime resetAt);
}
