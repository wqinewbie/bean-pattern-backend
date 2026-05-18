package com.beanpattern.controller;

import com.beanpattern.entity.VipPackage;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.VipPackageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vip-packages")
public class VipPackageController {

    private final VipPackageService vipPackageService;

    public VipPackageController(VipPackageService vipPackageService) {
        this.vipPackageService = vipPackageService;
    }

    @GetMapping
    public ApiResponse<List<VipPackage>> list() {
        return ApiResponse.ok(vipPackageService.listAllPackages());
    }

    @GetMapping("/{id}")
    public ApiResponse<VipPackage> getById(@PathVariable Long id) {
        VipPackage vipPackage = vipPackageService.getById(id);
        if (vipPackage == null) {
            return ApiResponse.fail("套餐不存在");
        }
        return ApiResponse.ok(vipPackage);
    }

    @PostMapping
    public ApiResponse<VipPackage> create(@RequestBody VipPackage vipPackage) {
        return ApiResponse.ok(vipPackageService.create(vipPackage));
    }

    @PutMapping("/{id}")
    public ApiResponse<VipPackage> update(@PathVariable Long id, @RequestBody VipPackage vipPackage) {
        vipPackage.setId(id);
        return ApiResponse.ok(vipPackageService.update(vipPackage));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        vipPackageService.updateStatus(id, isActive);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        vipPackageService.delete(id);
        return ApiResponse.ok(null);
    }
}
