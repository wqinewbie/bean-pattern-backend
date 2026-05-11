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

    /**
     * 插入领取记录
     */
    @Insert("INSERT INTO bp_user_checkin_claim(user_id, claim_date, continuous_days, " +
            "reward_type, reward_value, created_at) " +
            "VALUES(#{userId}, #{claimDate}, #{continuousDays}, #{rewardType}, " +
            "#{rewardValue}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserCheckinClaim claim);

    /**
     * 查询用户某天的领取记录
     */
    @Select("SELECT id, user_id AS userId, claim_date AS claimDate, " +
            "continuous_days AS continuousDays, reward_type AS rewardType, " +
            "reward_value AS rewardValue, created_at AS createdAt " +
            "FROM bp_user_checkin_claim " +
            "WHERE user_id = #{userId} AND claim_date = #{date}")
    UserCheckinClaim findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询用户最近的领取记录
     */
    @Select("SELECT id, user_id AS userId, claim_date AS claimDate, " +
            "continuous_days AS continuousDays, reward_type AS rewardType, " +
            "reward_value AS rewardValue, created_at AS createdAt " +
            "FROM bp_user_checkin_claim " +
            "WHERE user_id = #{userId} " +
            "ORDER BY claim_date DESC LIMIT #{limit}")
    List<UserCheckinClaim> findRecentByUser(@Param("userId") Long userId, @Param("limit") int limit);
}
