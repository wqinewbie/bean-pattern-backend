package com.beanpattern.controller;

import com.beanpattern.entity.GiftPackage;
import com.beanpattern.entity.GiftTypeConfig;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.GiftPackageService;
import com.beanpattern.service.GiftTypeConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminGiftController {

    private final GiftTypeConfigService giftTypeConfigService;
    private final GiftPackageService giftPackageService;

    public AdminGiftController(GiftTypeConfigService giftTypeConfigService, GiftPackageService giftPackageService) {
        this.giftTypeConfigService = giftTypeConfigService;
        this.giftPackageService = giftPackageService;
    }

    @GetMapping("/gift-types")
    public ApiResponse<List<GiftTypeConfig>> giftTypes(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return ApiResponse.ok(activeOnly ? giftTypeConfigService.listActive() : giftTypeConfigService.listAll());
    }

    @PostMapping("/gift-types")
    public ApiResponse<GiftTypeConfig> createGiftType(@RequestBody GiftTypeConfig giftTypeConfig) {
        giftTypeConfig.setId(null);
        return ApiResponse.ok(giftTypeConfigService.save(giftTypeConfig));
    }

    @PutMapping("/gift-types/{id}")
    public ApiResponse<GiftTypeConfig> updateGiftType(@PathVariable Long id, @RequestBody GiftTypeConfig giftTypeConfig) {
        giftTypeConfig.setId(id);
        return ApiResponse.ok(giftTypeConfigService.save(giftTypeConfig));
    }

    @PostMapping("/gift-types/{id}/toggle")
    public ApiResponse<Void> toggleGiftType(@PathVariable Long id) {
        giftTypeConfigService.toggleStatus(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/gift-packages")
    public ApiResponse<List<GiftPackage>> giftPackages(@RequestParam(defaultValue = "false") boolean activeOnly) {
        return ApiResponse.ok(activeOnly ? giftPackageService.listActive() : giftPackageService.listAll());
    }

    @PostMapping("/gift-packages")
    public ApiResponse<GiftPackage> createGiftPackage(@RequestBody GiftPackage giftPackage) {
        giftPackage.setId(null);
        return ApiResponse.ok(giftPackageService.save(giftPackage));
    }

    @PutMapping("/gift-packages/{id}")
    public ApiResponse<GiftPackage> updateGiftPackage(@PathVariable Long id, @RequestBody GiftPackage giftPackage) {
        giftPackage.setId(id);
        return ApiResponse.ok(giftPackageService.save(giftPackage));
    }

    @PostMapping("/gift-packages/{id}/toggle")
    public ApiResponse<Void> toggleGiftPackage(@PathVariable Long id) {
        giftPackageService.toggleStatus(id);
        return ApiResponse.ok(null);
    }
}
