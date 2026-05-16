package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.CardPackage;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.entity.PrivilegeConfig;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.entity.VipPackage;
import com.beanpattern.entity.VipProduct;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.OrderPaymentResponse;
import com.beanpattern.model.PaymentCreateResult;
import com.beanpattern.model.vo.VipInfoVO;
import com.beanpattern.service.CardPackageService;
import com.beanpattern.service.OrderService;
import com.beanpattern.service.PrivilegeConfigService;
import com.beanpattern.service.VipPackageService;
import com.beanpattern.service.VipService;
import com.beanpattern.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * VIP会员 Controller
 */
@RestController
@RequestMapping("/api/vip")
public class VipController {

    private final SessionHelper sessionHelper;
    private final VipService vipService;
    private final VipPackageService vipPackageService;
    private final CardPackageService cardPackageService;
    private final PrivilegeConfigService privilegeConfigService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public VipController(SessionHelper sessionHelper,
                         VipService vipService,
                         VipPackageService vipPackageService,
                         CardPackageService cardPackageService,
                         PrivilegeConfigService privilegeConfigService,
                         OrderService orderService,
                         PaymentService paymentService) {
        this.sessionHelper = sessionHelper;
        this.vipService = vipService;
        this.vipPackageService = vipPackageService;
        this.cardPackageService = cardPackageService;
        this.privilegeConfigService = privilegeConfigService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    /**
     * 获取VIP产品列表
     */
    @GetMapping("/products")
    public ApiResponse<List<VipProduct>> getProducts() {
        List<VipProduct> products = vipService.getActiveProducts();
        return ApiResponse.ok(products);
    }

    /**
     * 获取VIP产品详情
     */
    @GetMapping("/products/{productCode}")
    public ApiResponse<VipProduct> getProduct(@PathVariable String productCode) {
        VipProduct product = vipService.getProductByCode(productCode);
        if (product == null) {
            return ApiResponse.fail("产品不存在");
        }
        return ApiResponse.ok(product);
    }

    /**
     * 获取当前用户VIP状态
     */
    @GetMapping("/status")
    public ApiResponse<VipInfoVO> getVipStatus(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        int vipLevel = vipService.getUserVipLevel(user.getId());
        return ApiResponse.ok(VipInfoVO.from(user, vipLevel));
    }

    /**
     * 获取用户会员信息（前端兼容路径）
     */
    @GetMapping("/info")
    public ApiResponse<VipInfoVO> getVipInfo(HttpServletRequest request) {
        return getVipStatus(request);
    }

    /**
     * 获取VIP购买记录
     */
    @GetMapping("/records")
    public ApiResponse<List<?>> getVipRecords(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        return ApiResponse.ok(vipService.getUserVipHistory(user.getId()));
    }

    /**
     * 购买会员（兼容小程序旧路径）
     */
    @PostMapping("/purchase")
    public ApiResponse<OrderPaymentResponse> purchaseVip(HttpServletRequest request,
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
     * 获取会员套餐列表（新版）
     */
    @GetMapping("/packages")
    public ApiResponse<List<VipPackage>> getVipPackages(HttpServletRequest request) {
        try {
            // 尝试获取当前用户，如果未登录则返回null
            UserEntity user = sessionHelper.getUser(request);
            Long userId = user != null ? user.getId() : null;

            List<VipPackage> packages = vipPackageService.listActivePackagesForUser(userId);
            return ApiResponse.ok(packages);
        } catch (Exception e) {
            return ApiResponse.fail("获取会员套餐失败: " + e.getMessage());
        }
    }

    /**
     * 获取次卡套餐列表
     */
    @GetMapping("/card-packages")
    public ApiResponse<List<CardPackage>> getCardPackages(HttpServletRequest request) {
        try {
            // 尝试获取当前用户，如果未登录则返回null
            UserEntity user = sessionHelper.getUser(request);
            Long userId = user != null ? user.getId() : null;

            List<CardPackage> packages = cardPackageService.listActivePackagesForUser(userId);
            return ApiResponse.ok(packages);
        } catch (Exception e) {
            return ApiResponse.fail("获取次卡套餐失败: " + e.getMessage());
        }
    }

    /**
     * 获取权益对比表
     */
    @GetMapping("/privileges")
    public ApiResponse<List<PrivilegeConfig>> getPrivileges() {
        try {
            List<PrivilegeConfig> privileges = privilegeConfigService.listActiveConfigs();
            return ApiResponse.ok(privileges);
        } catch (Exception e) {
            return ApiResponse.fail("获取权益配置失败: " + e.getMessage());
        }
    }
}
