package com.beanpattern.controller;

import com.beanpattern.entity.CardPackage;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.CardPackageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 次卡接口（用户端）
 */
@RestController
@RequestMapping("/api/card")
public class CardController {

    private final CardPackageService cardPackageService;

    public CardController(CardPackageService cardPackageService) {
        this.cardPackageService = cardPackageService;
    }

    /**
     * 获取次卡套餐列表
     */
    @GetMapping("/packages")
    public ApiResponse<List<CardPackage>> getPackages() {
        try {
            List<CardPackage> packages = cardPackageService.listActivePackages();
            return ApiResponse.ok(packages);
        } catch (Exception e) {
            return ApiResponse.fail("获取次卡套餐失败: " + e.getMessage());
        }
    }
}
