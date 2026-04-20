package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.GiftItem;
import com.beanpattern.entity.GiftType;
import com.beanpattern.entity.UserGift;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.GiftService;
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
 * 礼品中心 Controller
 */
@RestController
@RequestMapping("/api/gift")
public class GiftController {

    private final SessionHelper sessionHelper;
    private final GiftService giftService;

    public GiftController(SessionHelper sessionHelper, GiftService giftService) {
        this.giftService = giftService;
        this.sessionHelper = sessionHelper;
    }

    /**
     * 获取礼品类型列表
     */
    @GetMapping("/types")
    public ApiResponse<List<GiftType>> getGiftTypes() {
        List<GiftType> types = giftService.getAllActiveTypes();
        return ApiResponse.ok(types);
    }

    /**
     * 获取礼品项列表
     */
    @GetMapping("/items")
    public ApiResponse<List<GiftItem>> getGiftItems() {
        List<GiftItem> items = giftService.getAvailableGifts();
        return ApiResponse.ok(items);
    }

    /**
     * 根据类型获取礼品
     */
    @GetMapping("/items/{typeId}")
    public ApiResponse<List<GiftItem>> getGiftItemsByType(@PathVariable Long typeId) {
        List<GiftItem> items = giftService.getAvailableGiftsByTypeId(typeId);
        return ApiResponse.ok(items);
    }

    /**
     * 获取用户礼品列表
     */
    @GetMapping("/my")
    public ApiResponse<List<UserGift>> getMyGifts(HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        List<UserGift> gifts = giftService.getUserGifts(user.getId());
        return ApiResponse.ok(gifts);
    }

    /**
     * 获取用户可用的礼品
     */
    @GetMapping("/available")
    public ApiResponse<List<UserGift>> getAvailableGifts(HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        List<UserGift> gifts = giftService.getAvailableUserGifts(user.getId());
        return ApiResponse.ok(gifts);
    }

    /**
     * 使用礼品
     */
    @PostMapping("/use")
    public ApiResponse<String> useGift(@RequestBody Map<String, Long> body,
                                       HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        Long giftId = body.get("giftId");
        if (giftId == null) {
            return ApiResponse.fail("礼品ID不能为空");
        }

        boolean success = giftService.useGift(user.getId(), giftId);
        if (success) {
            return ApiResponse.ok("使用成功");
        } else {
            return ApiResponse.fail("使用失败");
        }
    }
}
