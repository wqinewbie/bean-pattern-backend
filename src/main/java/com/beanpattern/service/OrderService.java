package com.beanpattern.service;

import com.beanpattern.entity.*;
import com.beanpattern.mapper.OrderMapper;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.mapper.UserGiftMapper;
import com.beanpattern.mapper.UserVipRecordMapper;
import com.beanpattern.model.PageResult;
import com.beanpattern.model.vo.OrderVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 订单服务
 */
@Service
public class OrderService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final UserGiftMapper userGiftMapper;
    private final UserVipRecordMapper userVipRecordMapper;
    private final VipPackageService vipPackageService;
    private final CardPackageService cardPackageService;
    private final VipService vipService;
    private final AiQuotaLogService aiQuotaLogService;
    private final InviteCodeService inviteCodeService;
    private final StringRedisTemplate redisTemplate;
    private final NotificationService notificationService;

    public OrderService(OrderMapper orderMapper,
                       UserMapper userMapper,
                       UserGiftMapper userGiftMapper,
                       UserVipRecordMapper userVipRecordMapper,
                       VipPackageService vipPackageService,
                       CardPackageService cardPackageService,
                       VipService vipService,
                       AiQuotaLogService aiQuotaLogService,
                       InviteCodeService inviteCodeService,
                       StringRedisTemplate redisTemplate,
                       NotificationService notificationService) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.userGiftMapper = userGiftMapper;
        this.userVipRecordMapper = userVipRecordMapper;
        this.vipPackageService = vipPackageService;
        this.cardPackageService = cardPackageService;
        this.vipService = vipService;
        this.aiQuotaLogService = aiQuotaLogService;
        this.inviteCodeService = inviteCodeService;
        this.redisTemplate = redisTemplate;
        this.notificationService = notificationService;
    }

    /**
     * 查询用户订单列表
     */
    public List<OrderEntity> getUserOrders(Long userId) {
        return orderMapper.listByUserId(userId);
    }

    /**
     * 分页查询用户订单列表
     */
    public PageResult<OrderVO> getUserOrdersPaged(Long userId, String productType, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 50);
        int offset = (safePage - 1) * safePageSize;
        long total = orderMapper.countByUserId(userId, productType);
        List<OrderVO> list = orderMapper.listByUserIdPaged(userId, productType, offset, safePageSize)
                .stream()
                .map(OrderVO::from)
                .collect(Collectors.toList());
        return PageResult.of(list, safePage, safePageSize, total);
    }

    /**
     * 查询当前用户单个订单
     */
    public OrderEntity getUserOrder(Long userId, String orderNo) {
        OrderEntity order = orderMapper.findByOrderNo(orderNo);
        if (order == null || !userId.equals(order.getUserId())) {
            return null;
        }
        return order;
    }

    /**
     * 取消当前用户待支付订单
     */
    @Transactional
    public void cancelUserOrder(Long userId, String orderNo) {
        OrderEntity order = getUserOrder(userId, orderNo);
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalArgumentException("当前订单状态不可取消");
        }
        orderMapper.updateStatus(order.getId(), "CANCELLED");
    }

    /**
     * 创建会员订单
     */
    @Transactional
    public OrderEntity createVipOrder(Long userId, String packageCode) {
        return createVipOrder(userId, packageCode, null);
    }

    /**
     * 创建会员订单（支持优惠券）
     */
    @Transactional
    public OrderEntity createVipOrder(Long userId, String packageCode, Long couponId) {
        // 查询套餐
        VipPackage vipPackage = vipPackageService.getByCode(packageCode);
        if (vipPackage == null || !vipPackage.getIsActive()) {
            throw new IllegalArgumentException("套餐不存在或已下架");
        }

        // 检查定时上下架
        LocalDateTime now = LocalDateTime.now();
        if (vipPackage.getShelfStartTime() != null && now.isBefore(vipPackage.getShelfStartTime())) {
            throw new IllegalArgumentException("套餐尚未上架");
        }
        if (vipPackage.getShelfEndTime() != null && now.isAfter(vipPackage.getShelfEndTime())) {
            throw new IllegalArgumentException("套餐已下架");
        }

        // 检查会员权益（vipOnly 限制）
        if (vipPackage.getVipOnly() != null && vipPackage.getVipOnly()) {
            if (!vipService.isVip(userId)) {
                throw new IllegalArgumentException("该套餐仅限会员购买");
            }
        }

        // 检查购买次数限制
        if (vipPackage.getPurchaseLimit() != null && vipPackage.getPurchaseLimit() > 0) {
            int purchaseCount = orderMapper.countUserPurchase(userId, "vip", packageCode);
            if (purchaseCount >= vipPackage.getPurchaseLimit()) {
                throw new IllegalArgumentException("已达到该套餐的购买次数限制");
            }
        }

        BigDecimal finalPrice = vipPackage.getPrice();

        // 如果使用优惠券，验证并计算折扣
        if (couponId != null) {
            UserGift coupon = validateAndGetCoupon(userId, couponId, "VIP_COUPON");
            finalPrice = applyDiscount(finalPrice, coupon.getValue());
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
        order.setAmount(finalPrice);
        order.setCouponId(couponId);
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
        return createCardOrder(userId, packageCode, null);
    }

    /**
     * 创建次卡订单（支持优惠券）
     */
    @Transactional
    public OrderEntity createCardOrder(Long userId, String packageCode, Long couponId) {
        // 查询套餐
        CardPackage cardPackage = cardPackageService.getByCode(packageCode);
        if (cardPackage == null || !cardPackage.getIsActive()) {
            throw new IllegalArgumentException("套餐不存在或已下架");
        }

        // 检查定时上下架
        LocalDateTime now = LocalDateTime.now();
        if (cardPackage.getShelfStartTime() != null && now.isBefore(cardPackage.getShelfStartTime())) {
            throw new IllegalArgumentException("套餐尚未上架");
        }
        if (cardPackage.getShelfEndTime() != null && now.isAfter(cardPackage.getShelfEndTime())) {
            throw new IllegalArgumentException("套餐已下架");
        }

        // 检查会员权益（vipOnly 限制）
        if (cardPackage.getVipOnly() != null && cardPackage.getVipOnly()) {
            if (!vipService.isVip(userId)) {
                throw new IllegalArgumentException("该套餐仅限会员购买");
            }
        }

        // 检查购买次数限制
        if (cardPackage.getPurchaseLimit() != null && cardPackage.getPurchaseLimit() > 0) {
            int purchaseCount = orderMapper.countUserPurchase(userId, "card", packageCode);
            if (purchaseCount >= cardPackage.getPurchaseLimit()) {
                throw new IllegalArgumentException("已达到该套餐的购买次数限制");
            }
        }

        // 检查用户是否是会员，决定使用会员价还是普通价
        boolean isVip = vipService.isVip(userId);

        BigDecimal price = isVip && cardPackage.getVipPrice() != null
                                && cardPackage.getVipPrice().compareTo(java.math.BigDecimal.ZERO) > 0
                          ? cardPackage.getVipPrice()
                          : cardPackage.getPrice();

        BigDecimal finalPrice = price;

        // 如果使用优惠券，验证并计算折扣
        if (couponId != null) {
            UserGift coupon = validateAndGetCoupon(userId, couponId, "CARD_COUPON", "VIP_CARD_COUPON");
            finalPrice = applyDiscount(finalPrice, coupon.getValue());
        }

        // 生成订单号
        String orderNo = generateOrderNo();

        // 创建订单
        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductType("card");
        order.setPackageCode(packageCode);
        order.setPlanName(cardPackage.getPackageName());
        order.setAmount(finalPrice);
        order.setCouponId(couponId);
        order.setStatus("PENDING");
        order.setExpireAt(LocalDateTime.now().plusMinutes(10));
        order.setDeliverStatus("PENDING");

        orderMapper.insert(order);
        return order;
    }

    /**
     * 验证并获取优惠券
     */
    private UserGift validateAndGetCoupon(Long userId, Long couponId, String... allowedCouponCodes) {
        UserGift coupon = userGiftMapper.findById(couponId);
        if (coupon == null) {
            throw new IllegalArgumentException("优惠券不存在");
        }
        if (!coupon.getUserId().equals(userId)) {
            throw new IllegalArgumentException("优惠券不属于当前用户");
        }
        if (coupon.getStatus() != 0) {
            throw new IllegalArgumentException("优惠券已使用或不可用");
        }
        if (coupon.getExpireAt() != null && coupon.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("优惠券已过期");
        }
        if (!"COUPON".equals(coupon.getGiftCategory())) {
            throw new IllegalArgumentException("该礼品不是优惠券");
        }

        // 检查优惠券类型是否匹配
        boolean typeMatched = false;
        for (String allowedCode : allowedCouponCodes) {
            if (allowedCode.equals(coupon.getGiftCode())) {
                typeMatched = true;
                break;
            }
        }
        if (!typeMatched) {
            throw new IllegalArgumentException("优惠券类型不适用于该商品");
        }

        return coupon;
    }

    /**
     * 应用折扣
     * @param originalPrice 原价
     * @param discountValue 折扣值（例如：80 表示8折）
     * @return 折扣后价格
     */
    private BigDecimal applyDiscount(BigDecimal originalPrice, Integer discountValue) {
        if (discountValue == null || discountValue <= 0 || discountValue >= 100) {
            return originalPrice;
        }
        BigDecimal discount = BigDecimal.valueOf(discountValue).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return originalPrice.multiply(discount).setScale(2, RoundingMode.HALF_UP);
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

        // 更新用户会员到期时间
        userMapper.updateVip(order.getUserId(), 1, newExpireAt);

        // 创建VIP记录（确保 isVip() 能够正确识别）
        UserVipRecord vipRecord = new UserVipRecord();
        vipRecord.setUserId(order.getUserId());
        vipRecord.setProductCode(order.getPackageCode());
        vipRecord.setVipLevel(1);
        vipRecord.setOrderId(order.getId());
        vipRecord.setOrderNo(order.getOrderNo());
        vipRecord.setStartAt(now);
        vipRecord.setExpireAt(newExpireAt);
        userVipRecordMapper.insert(vipRecord);

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

        // 发送VIP续费/开通通知
        try {
            notificationService.createVipRenewNotification(
                    order.getUserId(),
                    newExpireAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } catch (Exception ignored) {
            // 通知发送失败不影响主流程
        }
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
        giftPackageService.grantPackageToUser(order.getUserId(), order.getPackageCode(), "ORDER:" + order.getOrderNo());
    }

    /**
     * 支付回调处理（幂等性保证）
     */
    @Transactional
    public void handlePaymentCallback(String orderNo, String transactionId) {
        String lockKey = "order:pay:lock:" + orderNo;
        Boolean locked = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(locked)) {
            throw new RuntimeException("订单正在处理中");
        }

        try {
            OrderEntity order = orderMapper.findByOrderNo(orderNo);
            if (order == null) {
                throw new IllegalArgumentException("订单不存在");
            }

            if ("PAID".equals(order.getStatus())) {
                return;
            }

            order.setStatus("PAID");
            order.setTransactionId(transactionId);
            order.setPaidAt(LocalDateTime.now());
            orderMapper.updatePaymentSuccess(order.getId(), transactionId);

            try {
                deliverGoods(order);
                inviteCodeService.markInviteeFirstPaid(order.getUserId());

                if (order.getCouponId() != null) {
                    userGiftMapper.use(order.getCouponId());
                }

                orderMapper.updateDeliverStatus(order.getId(), "SUCCESS", null);
            } catch (Exception e) {
                orderMapper.updateDeliverStatus(order.getId(), "FAILED", e.getMessage());
                throw e;
            }

        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    public java.util.Map<String, Object> getPaymentStatus(String orderNo) {
        OrderEntity order = orderMapper.findByOrderNo(orderNo);
        if (order == null) {
            return java.util.Map.of("status", "NOT_FOUND");
        }
        return java.util.Map.of("status", order.getStatus(), "orderNo", order.getOrderNo());
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8);
    }
}
