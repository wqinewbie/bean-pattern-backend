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

/**
 * 订单相关接口（用户端）
 */
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

  /**
   * 获取当前用户订单列表
   */
  @GetMapping("/list")
  public ApiResponse<PageResult<OrderVO>> getOrderList(HttpServletRequest request,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "20") int pageSize,
                                                       @RequestParam(required = false) String productType) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            return ApiResponse.ok(orderService.getUserOrdersPaged(user.getId(), productType, page, pageSize));
        } catch (Exception e) {
            return ApiResponse.fail("加载订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建会员订单
     */
    @PostMapping("/vip")
    public ApiResponse<OrderPaymentResponse> createVipOrder(HttpServletRequest request,
                                                            @RequestBody Map<String, String> params) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            String packageCode = params.get("packageCode");

            if (packageCode == null || packageCode.isEmpty()) {
                return ApiResponse.fail("套餐代码不能为空");
            }

            OrderEntity order = orderService.createVipOrder(user.getId(), packageCode);
            PaymentCreateResult payment = paymentService.createPayment(order);
            OrderEntity currentOrder = orderService.getUserOrder(user.getId(), order.getOrderNo());
            return ApiResponse.ok(OrderPaymentResponse.from(currentOrder != null ? currentOrder : order, payment));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 创建次卡订单
     */
    @PostMapping("/card")
    public ApiResponse<OrderPaymentResponse> createCardOrder(HttpServletRequest request,
                                                             @RequestBody Map<String, String> params) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            String packageCode = params.get("packageCode");

            if (packageCode == null || packageCode.isEmpty()) {
                return ApiResponse.fail("套餐代码不能为空");
            }

            OrderEntity order = orderService.createCardOrder(user.getId(), packageCode);
            PaymentCreateResult payment = paymentService.createPayment(order);
            OrderEntity currentOrder = orderService.getUserOrder(user.getId(), order.getOrderNo());
            return ApiResponse.ok(OrderPaymentResponse.from(currentOrder != null ? currentOrder : order, payment));
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("创建订单失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderEntity> getOrderDetail(HttpServletRequest request,
                                                   @PathVariable String orderNo) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            OrderEntity order = orderService.getUserOrder(user.getId(), orderNo);
            if (order == null) {
                return ApiResponse.fail("订单不存在");
            }
            return ApiResponse.ok(order);
        } catch (Exception e) {
            return ApiResponse.fail("加载订单详情失败: " + e.getMessage());
        }
    }

    /**
     * 查询订单支付状态
     */
    @GetMapping("/{orderNo}/status")
    public ApiResponse<Map<String, Object>> getOrderStatus(HttpServletRequest request,
                                                           @PathVariable String orderNo) {
        try {
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
        } catch (Exception e) {
            return ApiResponse.fail("查询订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 取消待支付订单
     */
    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancelOrder(HttpServletRequest request,
                                         @PathVariable String orderNo) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            orderService.cancelUserOrder(user.getId(), orderNo);
            return ApiResponse.ok(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("取消订单失败: " + e.getMessage());
        }
    }
}
