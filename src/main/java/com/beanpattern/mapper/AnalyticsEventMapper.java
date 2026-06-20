package com.beanpattern.mapper;

import com.beanpattern.entity.AnalyticsEvent;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AnalyticsEventMapper {

    @Insert("INSERT IGNORE INTO bp_analytics_event (" +
            "client_event_id, event_name, event_time, server_time, user_id, session_id, page, refer_page, source, " +
            "is_login, is_vip, vip_level, pattern_id, pattern_source, result, fail_reason, params_json, created_at" +
            ") VALUES (" +
            "#{clientEventId}, #{eventName}, #{eventTime}, #{serverTime}, #{userId}, #{sessionId}, #{page}, #{referPage}, #{source}, " +
            "#{isLogin}, #{isVip}, #{vipLevel}, #{patternId}, #{patternSource}, #{result}, #{failReason}, #{paramsJson}, NOW()" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertIgnore(AnalyticsEvent event);

    @Select("<script>" +
            "SELECT id, client_event_id AS clientEventId, event_name AS eventName, event_time AS eventTime, " +
            "server_time AS serverTime, user_id AS userId, session_id AS sessionId, page, refer_page AS referPage, " +
            "source, is_login AS isLogin, is_vip AS isVip, vip_level AS vipLevel, pattern_id AS patternId, " +
            "pattern_source AS patternSource, result, fail_reason AS failReason, params_json AS paramsJson, created_at AS createdAt " +
            "FROM bp_analytics_event WHERE server_time &gt;= #{startAt} AND server_time &lt; #{endAt} " +
            "<if test='eventName != null and eventName != \"\"'>AND event_name = #{eventName} </if>" +
            "<if test='userId != null'>AND user_id = #{userId} </if>" +
            "<if test='pageName != null and pageName != \"\"'>AND page = #{pageName} </if>" +
            "ORDER BY server_time DESC LIMIT #{limit} OFFSET #{offset}" +
            "</script>")
    List<AnalyticsEvent> list(@Param("eventName") String eventName,
                              @Param("userId") Long userId,
                              @Param("pageName") String pageName,
                              @Param("startAt") LocalDateTime startAt,
                              @Param("endAt") LocalDateTime endAt,
                              @Param("limit") int limit,
                              @Param("offset") int offset);

    @Select("<script>" +
            "SELECT COUNT(*) FROM bp_analytics_event WHERE server_time &gt;= #{startAt} AND server_time &lt; #{endAt} " +
            "<if test='eventName != null and eventName != \"\"'>AND event_name = #{eventName} </if>" +
            "<if test='userId != null'>AND user_id = #{userId} </if>" +
            "<if test='pageName != null and pageName != \"\"'>AND page = #{pageName} </if>" +
            "</script>")
    long countList(@Param("eventName") String eventName,
                   @Param("userId") Long userId,
                   @Param("pageName") String pageName,
                   @Param("startAt") LocalDateTime startAt,
                   @Param("endAt") LocalDateTime endAt);

    @Select("SELECT COUNT(*) FROM bp_analytics_event WHERE server_time >= #{startAt} AND server_time < #{endAt}")
    long countAll(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    @Select("SELECT COUNT(DISTINCT user_id) FROM bp_analytics_event WHERE user_id IS NOT NULL AND server_time >= #{startAt} AND server_time < #{endAt}")
    long countUsers(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    @Select("SELECT COUNT(DISTINCT session_id) FROM bp_analytics_event WHERE server_time >= #{startAt} AND server_time < #{endAt}")
    long countSessions(@Param("startAt") LocalDateTime startAt, @Param("endAt") LocalDateTime endAt);

    @Select("SELECT COUNT(*) FROM bp_analytics_event WHERE event_name = #{eventName} AND server_time >= #{startAt} AND server_time < #{endAt}")
    long countByEvent(@Param("eventName") String eventName,
                      @Param("startAt") LocalDateTime startAt,
                      @Param("endAt") LocalDateTime endAt);

    @Select("SELECT COUNT(*) FROM bp_analytics_event WHERE event_name = #{eventName} AND result = #{result} AND server_time >= #{startAt} AND server_time < #{endAt}")
    long countByEventAndResult(@Param("eventName") String eventName,
                               @Param("result") String result,
                               @Param("startAt") LocalDateTime startAt,
                               @Param("endAt") LocalDateTime endAt);

    @Select("SELECT DATE(server_time) AS label, COUNT(*) AS value FROM bp_analytics_event " +
            "WHERE event_name = #{eventName} AND server_time >= #{startAt} AND server_time < #{endAt} " +
            "GROUP BY DATE(server_time) ORDER BY label")
    List<Map<String, Object>> trendByDay(@Param("eventName") String eventName,
                                         @Param("startAt") LocalDateTime startAt,
                                         @Param("endAt") LocalDateTime endAt);

    @Select("SELECT page AS label, COUNT(*) AS value FROM bp_analytics_event " +
            "WHERE event_name LIKE 'page\\_%\\_view' AND server_time >= #{startAt} AND server_time < #{endAt} " +
            "GROUP BY page ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> pageRank(@Param("startAt") LocalDateTime startAt,
                                       @Param("endAt") LocalDateTime endAt,
                                       @Param("limit") int limit);

    @Select("SELECT COALESCE(NULLIF(pattern_source, ''), 'unknown') AS label, COUNT(*) AS value FROM bp_analytics_event " +
            "WHERE pattern_source IS NOT NULL AND server_time >= #{startAt} AND server_time < #{endAt} " +
            "GROUP BY COALESCE(NULLIF(pattern_source, ''), 'unknown') ORDER BY value DESC")
    List<Map<String, Object>> patternSourceDistribution(@Param("startAt") LocalDateTime startAt,
                                                        @Param("endAt") LocalDateTime endAt);

    @Select("SELECT COALESCE(NULLIF(fail_reason, ''), 'unknown') AS label, COUNT(*) AS value FROM bp_analytics_event " +
            "WHERE fail_reason IS NOT NULL AND fail_reason <> '' AND server_time >= #{startAt} AND server_time < #{endAt} " +
            "GROUP BY COALESCE(NULLIF(fail_reason, ''), 'unknown') ORDER BY value DESC LIMIT #{limit}")
    List<Map<String, Object>> failureReasonRank(@Param("startAt") LocalDateTime startAt,
                                                @Param("endAt") LocalDateTime endAt,
                                                @Param("limit") int limit);
}
