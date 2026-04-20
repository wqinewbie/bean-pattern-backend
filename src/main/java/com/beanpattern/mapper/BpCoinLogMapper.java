package com.beanpattern.mapper;

import com.beanpattern.entity.BpCoinLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 金币日志 Mapper：bp_coin_log
 */
@Mapper
public interface BpCoinLogMapper {

    @Select("SELECT id, user_id AS userId, change_type AS changeType, change_amount AS changeAmount, " +
            "balance_before AS balanceBefore, balance_after AS balanceAfter, biz_type AS bizType, " +
            "biz_id AS bizId, description, created_at AS createdAt " +
            "FROM bp_coin_log WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<BpCoinLog> findByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Insert("INSERT INTO bp_coin_log(user_id, change_type, change_amount, balance_before, balance_after, biz_type, biz_id, description) " +
            "VALUES(#{userId}, #{changeType}, #{changeAmount}, #{balanceBefore}, #{balanceAfter}, #{bizType}, #{bizId}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BpCoinLog log);
}
