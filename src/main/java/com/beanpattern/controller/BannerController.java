package com.beanpattern.controller;

import com.beanpattern.mapper.BannerMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.vo.BannerVO;
import com.beanpattern.service.BannerService;
import com.beanpattern.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Banner接口
 * GET /api/banner/list - 获取首页轮播图列表
 * POST /api/banner/claim - 领取Banner礼品
 */
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    private final BannerMapper bannerMapper;
    private final BannerService bannerService;

    public BannerController(BannerMapper bannerMapper, BannerService bannerService) {
        this.bannerMapper = bannerMapper;
        this.bannerService = bannerService;
    }

    @GetMapping("/list")
    public ApiResponse<List<BannerVO>> list() {
        List<BannerVO> list = bannerMapper.listActive().stream()
                .map(BannerVO::from)
                .collect(Collectors.toList());
        return ApiResponse.ok(list);
    }

    /**
     * 领取Banner礼品
     */
    @PostMapping("/claim")
    public ApiResponse<Map<String, Object>> claimGift(@RequestHeader("Authorization") String token,
                                                       @RequestBody Map<String, Object> params) {
        try {
            Long userId = JwtUtil.getUserIdFromToken(token);
            Long bannerId = Long.valueOf(params.get("bannerId").toString());

            Map<String, Object> result = bannerService.claimBannerGift(userId, bannerId);
            return ApiResponse.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("领取失败：" + e.getMessage());
        }
    }
}
