package com.beanpattern.controller;

import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付回调接口
 */
@RestController
@RequestMapping("/api/pay")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final OrderService orderService;

    public PaymentController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 微信支付回调
     *
     * 注意：实际项目中需要：
     * 1. 验证微信签名
     * 2. 解密回调数据
     * 3. 返回微信要求的格式
     */
    @PostMapping("/wechat/notify")
    public Map<String, String> wechatPayNotify(@RequestBody Map<String, Object> params) {
        try {
            log.info("收到微信支付回调: {}", params);

            // TODO: 验证微信签名
            // TODO: 解密回调数据

            // 简化处理：直接从参数中获取订单号和交易号
            String orderNo = (String) params.get("out_trade_no");
            String transactionId = (String) params.get("transaction_id");

            if (orderNo == null || transactionId == null) {
                log.error("微信支付回调参数不完整");
                return Map.of("code", "FAIL", "message", "参数不完整");
            }

            // 处理支付回调
            orderService.handlePaymentCallback(orderNo, transactionId);

            // 返回成功
            return Map.of("code", "SUCCESS", "message", "OK");

        } catch (Exception e) {
            log.error("处理微信支付回调失败", e);
            return Map.of("code", "FAIL", "message", e.getMessage());
        }
    }

    /**
     * 查询订单支付状态（用于前端轮询）
     */
    @GetMapping("/status/{orderNo}")
    public ApiResponse<Map<String, Object>> getPaymentStatus(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.getPaymentStatus(orderNo));
    }
}
