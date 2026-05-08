package com.beanpattern.controller;

import com.beanpattern.entity.VipPackage;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.VipPackageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会员套餐配置管理接口（管理后台）
 */
@RestController
@RequestMapping("/api/admin/vip-packages")
public class VipPackageController {

    private final VipPackageService vipPackageService;

    public VipPackageController(VipPackageService vipPackageService) {
        this.vipPackageService = vipPackageService;
    }

    /**
     * 获取所有会员套餐
     */
    @GetMapping
    public ApiResponse<List<VipPackage>> list() {
        try {
            List<VipPackage> packages = vipPackageService.listAllPackages();
            return ApiResponse.ok(packages);
        } catch (Exception e) {
            return ApiResponse.fail("获取会员套餐列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个会员套餐
     */
    @GetMapping("/{id}")
    public ApiResponse<VipPackage> getById(@PathVariable Long id) {
        try {
            VipPackage vipPackage = vipPackageService.getById(id);
            if (vipPackage == null) {
                return ApiResponse.fail("套餐不存在");
            }
            return ApiResponse.ok(vipPackage);
        } catch (Exception e) {
            return ApiResponse.fail("获取会员套餐失败: " + e.getMessage());
        }
    }

    /**
     * 创建会员套餐
     */
    @PostMapping
    public ApiResponse<VipPackage> create(@RequestBody VipPackage vipPackage) {
        try {
            VipPackage created = vipPackageService.create(vipPackage);
            return ApiResponse.ok(created);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("创建会员套餐失败: " + e.getMessage());
        }
    }

    /**
     * 更新会员套餐
     */
    @PutMapping("/{id}")
    public ApiResponse<VipPackage> update(@PathVariable Long id, @RequestBody VipPackage vipPackage) {
        try {
            vipPackage.setId(id);
            VipPackage updated = vipPackageService.update(vipPackage);
            return ApiResponse.ok(updated);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("更新会员套餐失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用套餐
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        try {
            vipPackageService.updateStatus(id, isActive);
            return ApiResponse.ok(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("更新套餐状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除会员套餐
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            vipPackageService.delete(id);
            return ApiResponse.ok(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("删除会员套餐失败: " + e.getMessage());
        }
    }
}
