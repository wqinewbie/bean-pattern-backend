package com.beanpattern.service;

import com.beanpattern.entity.OrderEntity;
import com.beanpattern.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务服务
 */
@Service
public class OrderScheduledService {

    private static final Logger log = LoggerFactory.getLogger(OrderScheduledService.class);

    private final OrderMapper orderMapper;
    private final OrderService orderService;

    public OrderScheduledService(OrderMapper orderMapper, OrderService orderService) {
        this.orderMapper = orderMapper;
        this.orderService = orderService;
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
                    orderMapper.updateDeliverStatus(order.getId(), "FAILED", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("重试发货失败", e);
        }
    }
}
