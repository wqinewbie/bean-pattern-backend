package com.beanpattern.mapper;

import com.beanpattern.entity.OrderEntity;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface OrderMapper {

    @Select("SELECT COUNT(*) FROM bp_order")
    int count();

    @Select("SELECT COALESCE(SUM(amount), 0) FROM bp_order WHERE DATE(created_at) = CURDATE() AND status = 'PAID'")
    BigDecimal todayIncome();

    @Select("SELECT id, order_no AS orderNo, user_id AS userId, plan_id AS planId, plan_name AS planName, amount, status, created_at AS createdAt FROM bp_order ORDER BY created_at DESC LIMIT #{size} OFFSET #{offset}")
    List<OrderEntity> listAll(@Param("offset") int offset, @Param("size") int size);
}
