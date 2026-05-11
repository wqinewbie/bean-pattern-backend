package com.beanpattern.mapper;

import com.beanpattern.entity.UserCheckin;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户签到记录 Mapper（bp_user_checkin）
 */
@Mapper
public interface UserCheckinMapper {

    /**
     * 插入签到记录
     */
    @Insert("INSERT INTO bp_user_checkin(user_id, checkin_date, continuous_days, created_at) " +
            "VALUES(#{userId}, #{checkinDate}, #{continuousDays}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserCheckin checkin);

    /**
     * 查询用户某天的签到记录
     */
    @Select("SELECT id, user_id AS userId, checkin_date AS checkinDate, " +
            "continuous_days AS continuousDays, created_at AS createdAt " +
            "FROM bp_user_checkin " +
            "WHERE user_id = #{userId} AND checkin_date = #{date}")
    UserCheckin findByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询用户最近N天的签到记录
     */
    @Select("SELECT id, user_id AS userId, checkin_date AS checkinDate, " +
            "continuous_days AS continuousDays, created_at AS createdAt " +
            "FROM bp_user_checkin " +
            "WHERE user_id = #{userId} AND checkin_date >= #{startDate} " +
            "ORDER BY checkin_date DESC")
    List<UserCheckin> findRecentByUser(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    /**
     * 查询用户最后一次签到记录
     */
    @Select("SELECT id, user_id AS userId, checkin_date AS checkinDate, " +
            "continuous_days AS continuousDays, created_at AS createdAt " +
            "FROM bp_user_checkin " +
            "WHERE user_id = #{userId} " +
            "ORDER BY checkin_date DESC LIMIT 1")
    UserCheckin findLastByUser(@Param("userId") Long userId);
}
