package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.CardPackage;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.OrderPaymentResponse;
import com.beanpattern.model.PaymentCreateResult;
import com.beanpattern.service.CardPackageService;
import com.beanpattern.service.OrderService;
import com.beanpattern.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 次卡接口（用户端）
 */
@RestController
@RequestMapping("/api/card")
public class CardController {

    private final CardPackageService cardPackageService;
    private final SessionHelper sessionHelper;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public CardController(CardPackageService cardPackageService,
                          SessionHelper sessionHelper,
                          OrderService orderService,
                          PaymentService paymentService) {
        this.cardPackageService = cardPackageService;
        this.sessionHelper = sessionHelper;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    /**
     * 购买次卡（兼容小程序旧路径）
     */
    @PostMapping("/purchase")
    public ApiResponse<OrderPaymentResponse> purchaseCard(HttpServletRequest request,
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
}
