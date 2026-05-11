package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单相关接口（用户端）
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final SessionHelper sessionHelper;
    private final OrderService orderService;

    public OrderController(SessionHelper sessionHelper, OrderService orderService) {
        this.sessionHelper = sessionHelper;
        this.orderService = orderService;
    }

    /**
     * 获取当前用户订单列表
     */
    @GetMapping("/list")
    public ApiResponse<List<OrderEntity>> getOrderList(HttpServletRequest request) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            List<OrderEntity> orders = orderService.getUserOrders(user.getId());
            return ApiResponse.ok(orders);
        } catch (Exception e) {
            return ApiResponse.fail("加载订单列表失败: " + e.getMessage());
        }
    }

    /**
     * 创建会员订单
     */
    @PostMapping("/vip")
    public ApiResponse<OrderEntity> createVipOrder(HttpServletRequest request,
                                                   @RequestBody Map<String, String> params) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            String packageCode = params.get("packageCode");

            if (packageCode == null || packageCode.isEmpty()) {
                return ApiResponse.fail("套餐代码不能为空");
            }

            OrderEntity order = orderService.createVipOrder(user.getId(), packageCode);
            return ApiResponse.ok(order);
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
    public ApiResponse<OrderEntity> createCardOrder(HttpServletRequest request,
                                                    @RequestBody Map<String, String> params) {
        try {
            UserEntity user = sessionHelper.requireUser(request);
            String packageCode = params.get("packageCode");

            if (packageCode == null || packageCode.isEmpty()) {
                return ApiResponse.fail("套餐代码不能为空");
            }

            OrderEntity order = orderService.createCardOrder(user.getId(), packageCode);
            return ApiResponse.ok(order);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("创建订单失败: " + e.getMessage());
        }
    }
}
