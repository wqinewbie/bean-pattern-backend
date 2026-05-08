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

    @Select("SELECT id, order_no AS orderNo, user_id AS userId, plan_id AS planId, plan_name AS planName, " +
            "amount, status, product_id AS productId, vip_level_purchased AS vipLevelPurchased, " +
            "vip_days AS vipDays, gift_items AS giftItems, " +
            "product_type AS productType, package_code AS packageCode, expire_at AS expireAt, " +
            "deliver_status AS deliverStatus, deliver_error AS deliverError, " +
            "paid_at AS paidAt, transaction_id AS transactionId, " +
            "created_at AS createdAt FROM bp_order ORDER BY created_at DESC LIMIT #{size} OFFSET #{offset}")
    List<OrderEntity> listAll(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT id, order_no AS orderNo, user_id AS userId, plan_id AS planId, plan_name AS planName, " +
            "amount, status, product_id AS productId, vip_level_purchased AS vipLevelPurchased, " +
            "vip_days AS vipDays, gift_items AS giftItems, " +
            "product_type AS productType, package_code AS packageCode, expire_at AS expireAt, " +
            "deliver_status AS deliverStatus, deliver_error AS deliverError, " +
            "paid_at AS paidAt, transaction_id AS transactionId, " +
            "created_at AS createdAt FROM bp_order WHERE id = #{id}")
    OrderEntity findById(@Param("id") Long id);

    @Select("SELECT id, order_no AS orderNo, user_id AS userId, plan_id AS planId, plan_name AS planName, " +
            "amount, status, product_id AS productId, vip_level_purchased AS vipLevelPurchased, " +
            "vip_days AS vipDays, gift_items AS giftItems, " +
            "product_type AS productType, package_code AS packageCode, expire_at AS expireAt, " +
            "deliver_status AS deliverStatus, deliver_error AS deliverError, " +
            "paid_at AS paidAt, transaction_id AS transactionId, " +
            "created_at AS createdAt FROM bp_order WHERE order_no = #{orderNo}")
    OrderEntity findByOrderNo(@Param("orderNo") String orderNo);

    @Insert("INSERT INTO bp_order(order_no, user_id, product_type, package_code, " +
            "plan_name, amount, status, expire_at, deliver_status) " +
            "VALUES(#{orderNo}, #{userId}, #{productType}, #{packageCode}, " +
            "#{planName}, #{amount}, #{status}, #{expireAt}, #{deliverStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OrderEntity order);

    @Update("UPDATE bp_order SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE bp_order SET deliver_status = #{deliverStatus}, " +
            "deliver_error = #{deliverError} WHERE id = #{id}")
    int updateDeliverStatus(@Param("id") Long id,
                           @Param("deliverStatus") String deliverStatus,
                           @Param("deliverError") String deliverError);

    @Select("SELECT id, order_no AS orderNo, user_id AS userId, plan_id AS planId, plan_name AS planName, " +
            "amount, status, product_id AS productId, vip_level_purchased AS vipLevelPurchased, " +
            "vip_days AS vipDays, gift_items AS giftItems, " +
            "product_type AS productType, package_code AS packageCode, expire_at AS expireAt, " +
            "deliver_status AS deliverStatus, deliver_error AS deliverError, " +
            "paid_at AS paidAt, transaction_id AS transactionId, " +
            "created_at AS createdAt FROM bp_order " +
            "WHERE status = 'PENDING' AND expire_at < NOW() LIMIT #{limit}")
    List<OrderEntity> findExpiredOrders(@Param("limit") int limit);

    @Update("UPDATE bp_order SET status = 'TIMEOUT' " +
            "WHERE status = 'PENDING' AND expire_at < NOW()")
    int cancelExpiredOrders();

    @Select("SELECT id, order_no AS orderNo, user_id AS userId, plan_id AS planId, plan_name AS planName, " +
            "amount, status, product_id AS productId, vip_level_purchased AS vipLevelPurchased, " +
            "vip_days AS vipDays, gift_items AS giftItems, " +
            "product_type AS productType, package_code AS packageCode, expire_at AS expireAt, " +
            "deliver_status AS deliverStatus, deliver_error AS deliverError, " +
            "paid_at AS paidAt, transaction_id AS transactionId, " +
            "created_at AS createdAt FROM bp_order " +
            "WHERE status = 'PAID' AND deliver_status = 'FAILED' LIMIT #{limit}")
    List<OrderEntity> findFailedDeliveryOrders(@Param("limit") int limit);
}
