package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.OrderPaymentResponse;
import com.beanpattern.model.PageResult;
import com.beanpattern.model.PaymentCreateResult;
import com.beanpattern.model.vo.OrderVO;
import com.beanpattern.service.OrderService;
import com.beanpattern.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final SessionHelper sessionHelper;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderController(SessionHelper sessionHelper, OrderService orderService, PaymentService paymentService) {
        this.sessionHelper = sessionHelper;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @GetMapping("/list")
    public ApiResponse<PageResult<OrderVO>> getOrderList(HttpServletRequest request,
                                                         @RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "20") int pageSize,
                                                         @RequestParam(required = false) String productType) {
        UserEntity user = sessionHelper.requireUser(request);
        return ApiResponse.ok(orderService.getUserOrdersPaged(user.getId(), productType, page, pageSize));
    }

    @PostMapping("/vip")
    public ApiResponse<OrderPaymentResponse> createVipOrder(HttpServletRequest request,
                                                            @RequestBody Map<String, Object> params) {
        UserEntity user = sessionHelper.requireUser(request);
        String packageCode = (String) params.get("packageCode");
        Long couponId = toLong(params.get("couponId"));

        if (packageCode == null || packageCode.isEmpty()) {
            return ApiResponse.fail("套餐代码不能为空");
        }

        OrderEntity order = orderService.createVipOrder(user.getId(), packageCode, couponId);
        PaymentCreateResult payment = paymentService.createPayment(order);
        OrderEntity currentOrder = orderService.getUserOrder(user.getId(), order.getOrderNo());
        return ApiResponse.ok(OrderPaymentResponse.from(currentOrder != null ? currentOrder : order, payment));
    }

    @PostMapping("/card")
    public ApiResponse<OrderPaymentResponse> createCardOrder(HttpServletRequest request,
                                                             @RequestBody Map<String, Object> params) {
        UserEntity user = sessionHelper.requireUser(request);
        String packageCode = (String) params.get("packageCode");
        Long couponId = toLong(params.get("couponId"));

        if (packageCode == null || packageCode.isEmpty()) {
            return ApiResponse.fail("套餐代码不能为空");
        }

        OrderEntity order = orderService.createCardOrder(user.getId(), packageCode, couponId);
        PaymentCreateResult payment = paymentService.createPayment(order);
        OrderEntity currentOrder = orderService.getUserOrder(user.getId(), order.getOrderNo());
        return ApiResponse.ok(OrderPaymentResponse.from(currentOrder != null ? currentOrder : order, payment));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderEntity> getOrderDetail(HttpServletRequest request,
                                                   @PathVariable String orderNo) {
        UserEntity user = sessionHelper.requireUser(request);
        OrderEntity order = orderService.getUserOrder(user.getId(), orderNo);
        if (order == null) {
            return ApiResponse.fail("订单不存在");
        }
        return ApiResponse.ok(order);
    }

    @GetMapping("/{orderNo}/status")
    public ApiResponse<Map<String, Object>> getOrderStatus(HttpServletRequest request,
                                                           @PathVariable String orderNo) {
        UserEntity user = sessionHelper.requireUser(request);
        OrderEntity order = orderService.getUserOrder(user.getId(), orderNo);
        if (order == null) {
            return ApiResponse.fail("订单不存在");
        }
        return ApiResponse.ok(Map.of(
                "orderNo", order.getOrderNo(),
                "status", order.getStatus(),
                "deliverStatus", order.getDeliverStatus() != null ? order.getDeliverStatus() : ""
        ));
    }

    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancelOrder(HttpServletRequest request,
                                         @PathVariable String orderNo) {
        UserEntity user = sessionHelper.requireUser(request);
        orderService.cancelUserOrder(user.getId(), orderNo);
        return ApiResponse.ok(null);
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            String text = (String) value;
            if (!text.isBlank()) {
                try {
                    return Long.parseLong(text);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
