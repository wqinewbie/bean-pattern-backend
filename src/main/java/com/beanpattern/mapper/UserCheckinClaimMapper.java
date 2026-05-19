package com.beanpattern.mapper;

import com.beanpattern.entity.UserCheckinClaim;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 签到奖励领取记录 Mapper（bp_user_checkin_claim）
 */
@Mapper
public interface UserCheckinClaimMapper {

    @Insert("INSERT INTO bp_user_checkin_claim(user_id, claim_date, continuous_days, created_at) " +
            "VALUES(#{userId}, #{claimDate}, #{continuousDays}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserCheckinClaim claim);

    @Select("SELECT id, user_id AS userId, claim_date AS claimDate, " +
            "continuous_days AS continuousDays, created_at AS createdAt " +
            "FROM bp_user_checkin_claim " +
            "WHERE user_id = #{userId} AND claim_date = #{date}")
    UserCheckinClaim findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT id, user_id AS userId, claim_date AS claimDate, " +
            "continuous_days AS continuousDays, created_at AS createdAt " +
            "FROM bp_user_checkin_claim " +
            "WHERE user_id = #{userId} " +
            "ORDER BY claim_date DESC LIMIT #{limit}")
    List<UserCheckinClaim> findRecentByUser(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(1) FROM bp_user_checkin_claim WHERE claim_date = #{date}")
    int countByDate(@Param("date") LocalDate date);

    @Select("SELECT id, user_id AS userId, claim_date AS claimDate, " +
            "continuous_days AS continuousDays, created_at AS createdAt " +
            "FROM bp_user_checkin_claim WHERE user_id = #{userId} ORDER BY claim_date DESC LIMIT 1")
    UserCheckinClaim findLastByUser(@Param("userId") Long userId);

    @Select("SELECT COALESCE(SUM(al.reward_value), 0) FROM bp_user_activity_log al " +
            "WHERE al.action_type = 'CLAIM' AND al.activity_code LIKE 'checkin%'")
    int sumRewardValue();

    @Delete("DELETE FROM bp_user_checkin_claim WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
