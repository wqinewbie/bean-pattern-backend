package com.beanpattern.controller;

import com.beanpattern.entity.CardPackage;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.CardPackageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/card-packages")
public class CardPackageController {

    private final CardPackageService cardPackageService;

    public CardPackageController(CardPackageService cardPackageService) {
        this.cardPackageService = cardPackageService;
    }

    @GetMapping
    public ApiResponse<List<CardPackage>> list() {
        return ApiResponse.ok(cardPackageService.listAllPackages());
    }

    @GetMapping("/{id}")
    public ApiResponse<CardPackage> getById(@PathVariable Long id) {
        CardPackage cardPackage = cardPackageService.getById(id);
        if (cardPackage == null) {
            return ApiResponse.fail("套餐不存在");
        }
        return ApiResponse.ok(cardPackage);
    }

    @PostMapping
    public ApiResponse<CardPackage> create(@RequestBody CardPackage cardPackage) {
        return ApiResponse.ok(cardPackageService.create(cardPackage));
    }

    @PutMapping("/{id}")
    public ApiResponse<CardPackage> update(@PathVariable Long id, @RequestBody CardPackage cardPackage) {
        cardPackage.setId(id);
        return ApiResponse.ok(cardPackageService.update(cardPackage));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        cardPackageService.updateStatus(id, isActive);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        cardPackageService.delete(id);
        return ApiResponse.ok(null);
    }
}
