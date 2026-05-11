package com.beanpattern.mapper;

import com.beanpattern.entity.AiQuotaLog;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI次数变动日志表 Mapper（bp_ai_quota_log）
 */
@Mapper
public interface AiQuotaLogMapper {

    @Insert("INSERT INTO bp_ai_quota_log(user_id, change_type, change_amount, " +
            "balance_before, balance_after, biz_type, biz_id, description) " +
            "VALUES(#{userId}, #{changeType}, #{changeAmount}, #{balanceBefore}, " +
            "#{balanceAfter}, #{bizType}, #{bizId}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiQuotaLog log);

    @Select("SELECT id, user_id AS userId, change_type AS changeType, " +
            "change_amount AS changeAmount, balance_before AS balanceBefore, " +
            "balance_after AS balanceAfter, biz_type AS bizType, biz_id AS bizId, " +
            "description, created_at AS createdAt " +
            "FROM bp_ai_quota_log WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<AiQuotaLog> listByUserId(@Param("userId") Long userId,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM bp_ai_quota_log WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM bp_ai_quota_log WHERE user_id = #{userId} AND change_type = #{changeType} AND biz_type = #{bizType} AND biz_id = #{bizId}")
    int countByBiz(@Param("userId") Long userId,
                   @Param("changeType") String changeType,
                   @Param("bizType") String bizType,
                   @Param("bizId") String bizId);
}
