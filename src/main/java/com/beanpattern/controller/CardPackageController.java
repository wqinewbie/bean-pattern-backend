package com.beanpattern.controller;

import com.beanpattern.entity.CardPackage;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.CardPackageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 次卡套餐配置管理接口（管理后台）
 */
@RestController
@RequestMapping("/api/admin/card-packages")
public class CardPackageController {

    private final CardPackageService cardPackageService;

    public CardPackageController(CardPackageService cardPackageService) {
        this.cardPackageService = cardPackageService;
    }

    /**
     * 获取所有次卡套餐
     */
    @GetMapping
    public ApiResponse<List<CardPackage>> list() {
        try {
            List<CardPackage> packages = cardPackageService.listAllPackages();
            return ApiResponse.ok(packages);
        } catch (Exception e) {
            return ApiResponse.fail("获取次卡套餐列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个次卡套餐
     */
    @GetMapping("/{id}")
    public ApiResponse<CardPackage> getById(@PathVariable Long id) {
        try {
            CardPackage cardPackage = cardPackageService.getById(id);
            if (cardPackage == null) {
                return ApiResponse.fail("套餐不存在");
            }
            return ApiResponse.ok(cardPackage);
        } catch (Exception e) {
            return ApiResponse.fail("获取次卡套餐失败: " + e.getMessage());
        }
    }

    /**
     * 创建次卡套餐
     */
    @PostMapping
    public ApiResponse<CardPackage> create(@RequestBody CardPackage cardPackage) {
        try {
            CardPackage created = cardPackageService.create(cardPackage);
            return ApiResponse.ok(created);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("创建次卡套餐失败: " + e.getMessage());
        }
    }

    /**
     * 更新次卡套餐
     */
    @PutMapping("/{id}")
    public ApiResponse<CardPackage> update(@PathVariable Long id, @RequestBody CardPackage cardPackage) {
        try {
            cardPackage.setId(id);
            CardPackage updated = cardPackageService.update(cardPackage);
            return ApiResponse.ok(updated);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("更新次卡套餐失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用套餐
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        try {
            cardPackageService.updateStatus(id, isActive);
            return ApiResponse.ok(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("更新套餐状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除次卡套餐
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            cardPackageService.delete(id);
            return ApiResponse.ok(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("删除次卡套餐失败: " + e.getMessage());
        }
    }
}
