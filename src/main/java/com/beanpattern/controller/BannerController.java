package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.mapper.BannerMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.vo.BannerVO;
import com.beanpattern.service.BannerService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final SessionHelper sessionHelper;

    public BannerController(BannerMapper bannerMapper, BannerService bannerService, SessionHelper sessionHelper) {
        this.bannerMapper = bannerMapper;
        this.bannerService = bannerService;
        this.sessionHelper = sessionHelper;
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
    public ApiResponse<Map<String, Object>> claimGift(@RequestBody Map<String, Object> params,
                                                       HttpServletRequest request) {
        try {
            Long userId = sessionHelper.requireUser(request).getId();
            Long bannerId = Long.valueOf(params.get("bannerId").toString());

            Map<String, Object> result = bannerService.claimBannerGift(userId, bannerId);
            return ApiResponse.ok(result);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("领取失败：" + e.getMessage());
        }
    }
}
