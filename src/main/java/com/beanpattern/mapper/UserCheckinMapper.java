package com.beanpattern.mapper;

import com.beanpattern.entity.UserCheckin;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    @Select("<script>" +
            "SELECT c.id, c.user_id AS userId, c.checkin_date AS checkinDate, " +
            "c.continuous_days AS continuousDays, c.created_at AS createdAt, " +
            "u.nick_name AS nickName, u.avatar_url AS avatarUrl, u.phone AS phone, u.open_id AS openId " +
            "FROM bp_user_checkin c " +
            "LEFT JOIN bp_user u ON u.id = c.user_id " +
            "<where> " +
            "<if test='startDate != null'>AND c.checkin_date &gt;= #{startDate}</if> " +
            "<if test='endDate != null'>AND c.checkin_date &lt;= #{endDate}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (u.nick_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.open_id LIKE CONCAT('%', #{keyword}, '%') " +
            "OR CAST(c.user_id AS CHAR) = #{keyword})" +
            "</if> " +
            "</where> " +
            "ORDER BY c.checkin_date DESC, c.created_at DESC " +
            "LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<Map<String, Object>> findAdminRecent(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate,
                                               @Param("keyword") String keyword,
                                               @Param("limit") int limit,
                                               @Param("offset") int offset);

    @Select("<script>" +
            "SELECT COUNT(1) " +
            "FROM bp_user_checkin c " +
            "LEFT JOIN bp_user u ON u.id = c.user_id " +
            "<where> " +
            "<if test='startDate != null'>AND c.checkin_date &gt;= #{startDate}</if> " +
            "<if test='endDate != null'>AND c.checkin_date &lt;= #{endDate}</if> " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (u.nick_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.phone LIKE CONCAT('%', #{keyword}, '%') " +
            "OR u.open_id LIKE CONCAT('%', #{keyword}, '%') " +
            "OR CAST(c.user_id AS CHAR) = #{keyword})" +
            "</if> " +
            "</where>" +
            "</script>")
    int countAdminRecent(@Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate,
                         @Param("keyword") String keyword);

    @Select("SELECT COUNT(1) FROM bp_user_checkin WHERE checkin_date = #{date}")
    int countByDate(@Param("date") LocalDate date);

    @Select("SELECT COUNT(DISTINCT user_id) FROM bp_user_checkin")
    int countDistinctUsers();

    @Select("SELECT COUNT(1) FROM bp_user_checkin WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM bp_user_checkin WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
