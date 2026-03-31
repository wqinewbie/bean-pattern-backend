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
        return ApiResponse.ok(
                bannerMapper.listActive().stream()
                        .map(BannerVO::from)
                        .collect(Collectors.toList())
        );
    }
}
