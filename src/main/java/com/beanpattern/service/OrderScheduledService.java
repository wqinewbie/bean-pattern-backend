package com.beanpattern.service;

import com.beanpattern.entity.OrderEntity;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.OrderMapper;
import com.beanpattern.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单定时任务服务
 */
@Service
public class OrderScheduledService {

    private static final Logger log = LoggerFactory.getLogger(OrderScheduledService.class);

    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public OrderScheduledService(OrderMapper orderMapper,
                                 OrderService orderService,
                                 UserMapper userMapper,
                                 NotificationService notificationService) {
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    /**
     * 订单超时处理（每分钟执行一次）
     * 取消超时未支付的订单
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void cancelExpiredOrders() {
        try {
            log.info("开始处理超时订单");

            // 批量取消超时订单
            int count = orderMapper.cancelExpiredOrders();

            if (count > 0) {
                log.info("取消超时订单数量: {}", count);
            }

        } catch (Exception e) {
            log.error("处理超时订单失败", e);
        }
    }

    /**
     * 发货失败补偿（每5分钟执行一次）
     * 重试发货失败的订单
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void retryFailedDelivery() {
        try {
            log.info("开始重试发货失败的订单");

            // 查询发货失败的订单
            List<OrderEntity> failedOrders = orderMapper.findFailedDeliveryOrders(100);

            if (failedOrders.isEmpty()) {
                return;
            }

            log.info("发现发货失败订单数量: {}", failedOrders.size());

            // 重试发货
            for (OrderEntity order : failedOrders) {
                try {
                    orderService.deliverGoods(order);
                    orderMapper.updateDeliverStatus(order.getId(), "SUCCESS", null);
                    log.info("补偿发货成功: {}", order.getOrderNo());
                } catch (Exception e) {
                    log.error("补偿发货失败: {}", order.getOrderNo(), e);
                    // 更新错误信息
                    orderMapper.updateDeliverStatus(order.getId(), "FAILED", OrderService.summarizeDeliverError(e));
                }
            }

        } catch (Exception e) {
            log.error("重试发货失败", e);
        }
    }

    /**
     * VIP到期提醒（每天早上9点执行）
     * 查询未来3天内到期的VIP用户并发送通知
     */
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void sendVipExpireReminder() {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime start = now;
            LocalDateTime end = now.plusDays(3);

            List<UserEntity> expiringUsers = userMapper.findVipExpiringBetween(start, end);
            if (expiringUsers.isEmpty()) {
                return;
            }

            log.info("发现 {} 个VIP即将到期的用户，开始发送提醒", expiringUsers.size());

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            for (UserEntity user : expiringUsers) {
                try {
                    String expireDate = user.getVipExpireAt() != null
                            ? user.getVipExpireAt().format(fmt)
                            : "";
                    notificationService.createVipExpireNotification(user.getId(), expireDate);
                } catch (Exception e) {
                    log.error("发送VIP到期提醒失败: userId={}", user.getId(), e);
                }
            }

            log.info("VIP到期提醒发送完成");
        } catch (Exception e) {
            log.error("VIP到期提醒任务失败", e);
        }
    }
}
