package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.entity.VipProduct;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.VipService;
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

    public VipController(SessionHelper sessionHelper, VipService vipService) {
        this.sessionHelper = sessionHelper;
        this.vipService = vipService;
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
    public ApiResponse<Map<String, Object>> getVipStatus(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        int vipLevel = vipService.getUserVipLevel(user.getId());
        return ApiResponse.ok(Map.of(
                "vipLevel", vipLevel,
                "vipExpireAt", user.getVipExpireAt() != null ? user.getVipExpireAt().toString() : "",
                "storageQuota", user.getStorageQuota() != null ? user.getStorageQuota() : 10,
                "draftQuota", user.getDraftQuota() != null ? user.getDraftQuota() : 20,
                "aiQuota", user.getAiQuota() != null ? user.getAiQuota() : 3,
                "availableBrands", user.getAvailableBrands() != null ? user.getAvailableBrands() : "[]"
        ));
    }

    /**
     * 获取VIP购买记录
     */
    @GetMapping("/records")
    public ApiResponse<List<?>> getVipRecords(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        return ApiResponse.ok(vipService.getUserVipHistory(user.getId()));
    }
}
