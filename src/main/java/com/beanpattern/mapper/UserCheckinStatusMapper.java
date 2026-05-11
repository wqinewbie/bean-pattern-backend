package com.beanpattern.mapper;

import com.beanpattern.entity.UserCheckinStatus;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;

/**
 * 用户签到状态 Mapper（bp_user_checkin_status）
 */
@Mapper
public interface UserCheckinStatusMapper {

    /**
     * 查询用户签到状态
     */
    @Select("SELECT id, user_id AS userId, continuous_days AS continuousDays, " +
            "total_days AS totalDays, last_checkin_date AS lastCheckinDate, " +
            "can_claim AS canClaim, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_checkin_status WHERE user_id = #{userId}")
    UserCheckinStatus findByUserId(@Param("userId") Long userId);

    /**
     * 插入签到状态
     */
    @Insert("INSERT INTO bp_user_checkin_status(user_id, continuous_days, total_days, " +
            "last_checkin_date, can_claim, created_at, updated_at) " +
            "VALUES(#{userId}, #{continuousDays}, #{totalDays}, #{lastCheckinDate}, " +
            "#{canClaim}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserCheckinStatus status);

    /**
     * 更新签到状态
     */
    @Update("UPDATE bp_user_checkin_status SET " +
            "continuous_days = #{continuousDays}, " +
            "total_days = #{totalDays}, " +
            "last_checkin_date = #{lastCheckinDate}, " +
            "can_claim = #{canClaim}, " +
            "updated_at = NOW() " +
            "WHERE user_id = #{userId}")
    int update(UserCheckinStatus status);

    /**
     * 重置连续天数和领取状态
     */
    @Update("UPDATE bp_user_checkin_status SET " +
            "continuous_days = 0, " +
            "can_claim = 0, " +
            "updated_at = NOW() " +
            "WHERE user_id = #{userId}")
    int resetContinuous(@Param("userId") Long userId);
}
