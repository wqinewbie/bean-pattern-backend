package com.beanpattern.controller;

import com.beanpattern.mapper.BannerMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.model.vo.BannerVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Banner接口
 * GET /api/banner/list - 获取首页轮播图列表
 */
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    private final BannerMapper bannerMapper;

    public BannerController(BannerMapper bannerMapper) {
        this.bannerMapper = bannerMapper;
    }

    @GetMapping("/list")
    public ApiResponse<List<BannerVO>> list() {
        List<BannerVO> list = bannerMapper.listActive().stream()
                .map(BannerVO::from)
                .collect(Collectors.toList());

        // 兼容老数据：若 bgColor 为空则给默认值
        for (BannerVO vo : list) {
            if (vo.getBgColor() == null || vo.getBgColor().trim().isEmpty()) {
                vo.setBgColor("#FF9800");
            }
        }
        return ApiResponse.ok(list);
    }
}
