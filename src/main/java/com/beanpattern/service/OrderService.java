package com.beanpattern.service;

import com.beanpattern.entity.*;
import com.beanpattern.mapper.OrderMapper;
import com.beanpattern.mapper.UserMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务
 */
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final VipPackageService vipPackageService;
    private final CardPackageService cardPackageService;
    private final AiQuotaLogService aiQuotaLogService;
    private final InviteCodeService inviteCodeService;
    private final StringRedisTemplate redisTemplate;

    public OrderService(OrderMapper orderMapper,
                       UserMapper userMapper,
                       VipPackageService vipPackageService,
                       CardPackageService cardPackageService,
                       AiQuotaLogService aiQuotaLogService,
                       InviteCodeService inviteCodeService,
                       StringRedisTemplate redisTemplate) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.vipPackageService = vipPackageService;
        this.cardPackageService = cardPackageService;
        this.aiQuotaLogService = aiQuotaLogService;
        this.inviteCodeService = inviteCodeService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 查询用户订单列表
     */
    public List<OrderEntity> getUserOrders(Long userId) {
        return orderMapper.listByUserId(userId);
    }

    /**
     * 创建会员订单
     */
    @Transactional
    public OrderEntity createVipOrder(Long userId, String packageCode) {
        // 查询套餐
        VipPackage vipPackage = vipPackageService.getByCode(packageCode);
        if (vipPackage == null || !vipPackage.getIsActive()) {
            throw new IllegalArgumentException("套餐不存在或已下架");
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductType("vip");
        order.setPackageCode(packageCode);
        order.setPlanName(vipPackage.getPackageName());
        order.setAmount(vipPackage.getPrice());
        order.setStatus("PENDING");
        order.setExpireAt(LocalDateTime.now().plusMinutes(10));
        order.setDeliverStatus("PENDING");

        orderMapper.insert(order);
        return order;
    }

    /**
     * 创建次卡订单
     */
    @Transactional
    public OrderEntity createCardOrder(Long userId, String packageCode) {
        // 查询套餐
        CardPackage cardPackage = cardPackageService.getByCode(packageCode);
        if (cardPackage == null || !cardPackage.getIsActive()) {
            throw new IllegalArgumentException("套餐不存在或已下架");
        }

        // 检查用户是否是会员，决定使用会员价还是普通价
        UserEntity user = userMapper.findById(userId);
        boolean isVip = user.getVipExpireAt() != null &&
                       user.getVipExpireAt().isAfter(LocalDateTime.now());

        BigDecimal price = isVip && cardPackage.getVipPrice() != null
                          ? cardPackage.getVipPrice()
                          : cardPackage.getPrice();

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductType("card");
        order.setPackageCode(packageCode);
        order.setPlanName(cardPackage.getPackageName());
        order.setAmount(price);
        order.setStatus("PENDING");
        order.setExpireAt(LocalDateTime.now().plusMinutes(10));
        order.setDeliverStatus("PENDING");

        orderMapper.insert(order);
        return order;
    }

    /**
     * 发货：开通会员或增加AI次数
     */
    @Transactional
    public void deliverGoods(OrderEntity order) {
        // 根据订单类型发货
        String productType = order.getProductType();

        if ("vip".equals(productType)) {
            deliverVip(order);
        } else if ("card".equals(productType)) {
            deliverCard(order);
        } else if ("gift".equals(productType)) {
            deliverGift(order);
        }
    }

    /**
     * 发货：开通会员
     */
    private void deliverVip(OrderEntity order) {
        // 根据订单的packageCode获取套餐信息
        VipPackage vipPackage = vipPackageService.getByCode(order.getPackageCode());
        if (vipPackage == null) {
            throw new IllegalArgumentException("套餐不存在");
        }

        // 获取用户当前会员到期时间
        UserEntity user = userMapper.findById(order.getUserId());
        LocalDateTime currentExpireAt = user.getVipExpireAt();
        LocalDateTime now = LocalDateTime.now();

        // 计算新的到期时间（从当前到期时间开始累加，最多365天）
        LocalDateTime baseTime = (currentExpireAt != null && currentExpireAt.isAfter(now))
                                ? currentExpireAt
                                : now;

        LocalDateTime newExpireAt = baseTime.plusDays(vipPackage.getDurationDays());

        // 检查是否超过最大叠加限制（365天）
        if (newExpireAt.isAfter(now.plusDays(365))) {
            newExpireAt = now.plusDays(365);
        }

        // 更新用户会员到期时间
        userMapper.updateVip(order.getUserId(), 1, newExpireAt);

        // 赠送AI次数
        int aiQuotaGift = vipPackage.getAiQuotaGift() != null ? vipPackage.getAiQuotaGift() : 10;
        userMapper.addAiQuota(order.getUserId(), aiQuotaGift);

        // 记录AI次数变动日志
        aiQuotaLogService.logChange(
            order.getUserId(),
            "PURCHASE",
            aiQuotaGift,
            "ORDER",
            order.getOrderNo(),
            "购买会员赠送AI次数"
        );
    }

    /**
     * 发货：增加AI次数
     */
    private void deliverCard(OrderEntity order) {
        // 根据订单的packageCode获取套餐信息
        CardPackage cardPackage = cardPackageService.getByCode(order.getPackageCode());
        if (cardPackage == null) {
            throw new IllegalArgumentException("套餐不存在");
        }

        // 增加AI次数
        int aiQuota = cardPackage.getAiQuota();
        userMapper.addAiQuota(order.getUserId(), aiQuota);

        // 记录AI次数变动日志
        aiQuotaLogService.logChange(
            order.getUserId(),
            "PURCHASE",
            aiQuota,
            "ORDER",
            order.getOrderNo(),
            "购买次卡"
        );
    }

    /**
     * 发货：礼品订单
     */
    private void deliverGift(OrderEntity order) {
        // 礼品订单的发货逻辑
        // 根据礼品类型进行不同的处理
    }

    /**
     * 支付回调处理（幂等性保证）
     */
    @Transactional
    public void handlePaymentCallback(String orderNo, String transactionId) {
        // 1. 检查订单状态
        OrderEntity order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        if ("PAID".equals(order.getStatus())) {
            // 订单已支付，直接返回
            return;
        }

        // 2. 使用分布式锁
        String lockKey = "order:pay:lock:" + orderNo;
        Boolean locked = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(locked)) {
            throw new RuntimeException("订单正在处理中");
        }

        try {
            // 3. 双重检查订单状态
            order = orderMapper.findByOrderNo(orderNo);
            if (!"PENDING".equals(order.getStatus())) {
                return;
            }

            // 4. 更新订单状态为已支付
            order.setStatus("PAID");
            order.setTransactionId(transactionId);
            order.setPaidAt(LocalDateTime.now());
            orderMapper.updateStatus(order.getId(), "PAID");

            // 5. 同步发货
            try {
                deliverGoods(order);
                inviteCodeService.markInviteeFirstPaid(order.getUserId());
                orderMapper.updateDeliverStatus(order.getId(), "SUCCESS", null);
            } catch (Exception e) {
                // 发货失败，标记状态
                orderMapper.updateDeliverStatus(order.getId(), "FAILED", e.getMessage());
                throw e;
            }

        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }
}
