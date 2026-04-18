package com.beanpattern.controller;

import com.beanpattern.entity.WatermarkConfig;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.WatermarkConfigService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watermark")
public class WatermarkConfigController {

    private final WatermarkConfigService service;

    public WatermarkConfigController(WatermarkConfigService service) {
        this.service = service;
    }

    /**
     * GET /api/watermark/config
     * 获取水印配置
     */
    @GetMapping("/config")
    public ApiResponse<WatermarkConfig> getConfig() {
        return ApiResponse.ok(service.getConfig());
    }

    /**
     * POST /api/watermark/config
     * 保存水印配置
     */
    @PostMapping("/config")
    public ApiResponse<WatermarkConfig> saveConfig(@RequestBody WatermarkConfig config) {
        service.save(config);
        return ApiResponse.ok(config);
    }
}
