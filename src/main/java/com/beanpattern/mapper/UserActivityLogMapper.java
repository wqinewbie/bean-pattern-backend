package com.beanpattern.mapper;

import com.beanpattern.entity.UserActivityLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户活动参与记录 Mapper：bp_user_activity_log
 */
@Mapper
public interface UserActivityLogMapper {

    /**
     * 插入参与记录
     */
    @Insert("INSERT INTO bp_user_activity_log (user_id, activity_id, activity_code, action_type, " +
            "reward_type, reward_value, gift_id, created_at) " +
            "VALUES (#{userId}, #{activityId}, #{activityCode}, #{actionType}, " +
            "#{rewardType}, #{rewardValue}, #{giftId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserActivityLog log);

    /**
     * 查询用户是否参与过某个活动
     */
    @Select("SELECT id, user_id AS userId, activity_id AS activityId, activity_code AS activityCode, " +
            "action_type AS actionType, reward_type AS rewardType, reward_value AS rewardValue, " +
            "gift_id AS giftId, created_at AS createdAt " +
            "FROM bp_user_activity_log " +
            "WHERE user_id = #{userId} AND activity_id = #{activityId} AND action_type = #{actionType}")
    UserActivityLog findByUserAndActivityAndAction(@Param("userId") Long userId,
                                                    @Param("activityId") Long activityId,
                                                    @Param("actionType") String actionType);

    @Select("SELECT COUNT(*) FROM bp_user_activity_log WHERE user_id = #{userId} AND activity_id = #{activityId} AND action_type = #{actionType}")
    int countByUserAndActivityAndAction(@Param("userId") Long userId,
                                         @Param("activityId") Long activityId,
                                         @Param("actionType") String actionType);

    @Select("SELECT COUNT(*) FROM bp_user_activity_log WHERE user_id = #{userId} AND activity_id = #{activityId} AND action_type = #{actionType} AND DATE(created_at) = CURDATE()")
    int countTodayByUserAndActivityAndAction(@Param("userId") Long userId,
                                              @Param("activityId") Long activityId,
                                              @Param("actionType") String actionType);

    /**
     * 查询用户的活动参与记录
     */
    @Select("SELECT id, user_id AS userId, activity_id AS activityId, activity_code AS activityCode, " +
            "action_type AS actionType, reward_type AS rewardType, reward_value AS rewardValue, " +
            "gift_id AS giftId, created_at AS createdAt " +
            "FROM bp_user_activity_log " +
            "WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT 200")
    List<UserActivityLog> findByUserId(@Param("userId") Long userId);

    /**
     * 查询活动的参与记录
     */
    @Select("SELECT id, user_id AS userId, activity_id AS activityId, activity_code AS activityCode, " +
            "action_type AS actionType, reward_type AS rewardType, reward_value AS rewardValue, " +
            "gift_id AS giftId, created_at AS createdAt " +
            "FROM bp_user_activity_log " +
            "WHERE activity_id = #{activityId} ORDER BY created_at DESC LIMIT 200")
    List<UserActivityLog> findByActivityId(@Param("activityId") Long activityId);
}
