package com.beanpattern.mapper;

import com.beanpattern.entity.RechargePlanEntity;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RechargePlanMapper {

    @Select("SELECT id, name, description, coins, ai_quota AS aiQuota, price, " +
            "original_price AS originalPrice, is_vip AS isVip, vip_days AS vipDays, " +
            "tag, sort_order AS sortOrder, status " +
            "FROM bp_recharge_plan WHERE status = 1 ORDER BY sort_order ASC")
    List<RechargePlanEntity> listActive();

    @Select("SELECT id, name, description, coins, ai_quota AS aiQuota, price, " +
            "original_price AS originalPrice, is_vip AS isVip, vip_days AS vipDays, " +
            "tag, sort_order AS sortOrder, status " +
            "FROM bp_recharge_plan ORDER BY sort_order ASC")
    List<RechargePlanEntity> listAll();

    @Insert("INSERT INTO bp_recharge_plan(name, description, coins, ai_quota, price, original_price, " +
            "is_vip, vip_days, tag, sort_order, status) " +
            "VALUES(#{name}, #{description}, #{coins}, #{aiQuota}, #{price}, #{originalPrice}, " +
            "#{isVip}, #{vipDays}, #{tag}, #{sortOrder}, 1)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RechargePlanEntity plan);

    @Update("UPDATE bp_recharge_plan SET name=#{name}, description=#{description}, coins=#{coins}, " +
            "ai_quota=#{aiQuota}, price=#{price}, original_price=#{originalPrice}, " +
            "is_vip=#{isVip}, vip_days=#{vipDays}, tag=#{tag}, sort_order=#{sortOrder} " +
            "WHERE id=#{id}")
    int update(RechargePlanEntity plan);

    @Update("UPDATE bp_recharge_plan SET status = CASE WHEN status=1 THEN 0 ELSE 1 END WHERE id=#{id}")
    int toggleStatus(@Param("id") Long id);
}
