package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.CardPackage;
import com.beanpattern.entity.OrderEntity;
import com.beanpattern.entity.PrivilegeConfig;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.entity.VipPackage;
import com.beanpattern.entity.VipProduct;
import com.beanpattern.model.ApiResponse;
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
        boolean isVip = vipService.isVip(user.getId());
        return ApiResponse.ok(VipInfoVO.from(user, vipLevel, isVip));
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
     * 获取会员套餐列表（新版）
     */
    @GetMapping("/packages")
    public ApiResponse<List<VipPackage>> getVipPackages(HttpServletRequest request) {
        UserEntity user = sessionHelper.getUser(request);
        Long userId = user != null ? user.getId() : null;
        return ApiResponse.ok(vipPackageService.listActivePackagesForUser(userId));
    }

    @GetMapping("/card-packages")
    public ApiResponse<List<CardPackage>> getCardPackages(HttpServletRequest request) {
        UserEntity user = sessionHelper.getUser(request);
        Long userId = user != null ? user.getId() : null;
        return ApiResponse.ok(cardPackageService.listActivePackagesForUser(userId));
    }

    @GetMapping("/privileges")
    public ApiResponse<List<PrivilegeConfig>> getPrivileges() {
        return ApiResponse.ok(privilegeConfigService.listActiveConfigs());
    }
}
