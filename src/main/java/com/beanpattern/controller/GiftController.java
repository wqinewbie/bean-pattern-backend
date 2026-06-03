package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.GiftItem;
import com.beanpattern.entity.GiftType;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.mapper.UserMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.vo.GiftVO;
import com.beanpattern.service.GiftService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 礼品中心 Controller
 */
@RestController
@RequestMapping("/api/gift")
public class GiftController {

    private final SessionHelper sessionHelper;
    private final GiftService giftService;
    private final UserMapper userMapper;

    public GiftController(SessionHelper sessionHelper, GiftService giftService, UserMapper userMapper) {
        this.giftService = giftService;
        this.sessionHelper = sessionHelper;
        this.userMapper = userMapper;
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
    public ApiResponse<List<GiftVO>> getMyGifts(HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        List<GiftVO> gifts = giftService.getUserGifts(user.getId()).stream().map(GiftVO::from).collect(java.util.stream.Collectors.toList());
        return ApiResponse.ok(gifts);
    }

    @GetMapping("/available")
    public ApiResponse<List<GiftVO>> getAvailableGifts(HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        List<GiftVO> gifts = giftService.getAvailableUserGifts(user.getId()).stream().map(GiftVO::from).collect(java.util.stream.Collectors.toList());
        return ApiResponse.ok(gifts);
    }

    /**
     * 获取用户可用的优惠券（按商品类型过滤）
     * @param productType 商品类型：vip/card
     */
    @GetMapping("/coupons/{productType}")
    public ApiResponse<List<GiftVO>> getAvailableCoupons(@PathVariable String productType,
                                                          HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        List<GiftVO> coupons = giftService.getAvailableCoupons(user.getId(), productType);
        return ApiResponse.ok(coupons);
    }

    /**
     * 使用礼品
     */
    @PostMapping("/use")
    public ApiResponse<Map<String, Object>> useGift(@RequestBody Map<String, Object> body,
                                                    HttpServletRequest request) {
        com.beanpattern.entity.UserEntity user = sessionHelper.requireUser(request);
        Long giftId = toLong(body.get("giftId"));
        if (giftId == null) {
            return ApiResponse.fail("礼品ID不能为空");
        }

        boolean redeemNow = Boolean.TRUE.equals(body.get("redeemNow"));
        boolean success = giftService.useGift(user.getId(), giftId, redeemNow);
        if (success) {
            UserEntity latestUser = userMapper.findById(user.getId());
            Map<String, Object> result = new HashMap<>();
            result.put("message", redeemNow ? "兑换成功" : "使用成功");
            result.put("user", buildUserBenefitSnapshot(latestUser));
            return ApiResponse.ok(result);
        } else {
            return ApiResponse.fail(redeemNow ? "兑换失败" : "使用失败");
        }
    }

    private Map<String, Object> buildUserBenefitSnapshot(UserEntity user) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("aiQuota", user != null && user.getAiQuota() != null ? user.getAiQuota() : 0);
        snapshot.put("vipLevel", user != null && user.getVipLevel() != null ? user.getVipLevel() : 0);
        snapshot.put("vipExpireAt", user != null && user.getVipExpireAt() != null ? user.getVipExpireAt().toString() : null);
        return snapshot;
    }

    private Long toLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            String text = (String) value;
            if (!text.isBlank()) {
                return Long.parseLong(text);
            }
        }
        return null;
    }
}
