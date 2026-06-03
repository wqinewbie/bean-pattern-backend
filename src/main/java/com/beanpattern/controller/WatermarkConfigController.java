package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.WatermarkConfig;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.WatermarkConfigService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/watermark")
public class WatermarkConfigController {

    private final WatermarkConfigService service;
    private final SessionHelper sessionHelper;

    public WatermarkConfigController(WatermarkConfigService service, SessionHelper sessionHelper) {
        this.service = service;
        this.sessionHelper = sessionHelper;
    }

    /**
     * GET /api/watermark/config
     * 获取全局水印配置（管理后台用）
     */
    @GetMapping("/config")
    public ApiResponse<WatermarkConfig> getConfig() {
        return ApiResponse.ok(service.getConfig());
    }

    /**
     * POST /api/watermark/config
     * 保存全局水印配置（管理后台用）
     */
    @PostMapping("/config")
    public ApiResponse<WatermarkConfig> saveConfig(@RequestBody WatermarkConfig config) {
        service.save(config);
        return ApiResponse.ok(config);
    }

    /**
     * GET /api/watermark/user-config
     * 获取用户水印配置（小程序端用）
     */
    @GetMapping("/user-config")
    public ApiResponse<Map<String, Object>> getUserConfig(HttpServletRequest request) {
        Long userId = sessionHelper.requireUser(request).getId();
        return ApiResponse.ok(service.getUserConfig(userId));
    }

    /**
     * POST /api/watermark/user-config
     * 保存用户水印配置（小程序端用，VIP专属）
     */
    @PostMapping("/user-config")
    public ApiResponse<String> saveUserConfig(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = sessionHelper.requireUser(request).getId();
        Integer enabled = parseEnabled(params != null ? params.get("enabled") : null);
        String customText = params != null && params.get("customText") != null
                ? String.valueOf(params.get("customText"))
                : null;
        
        service.saveUserConfig(userId, enabled, customText);
        return ApiResponse.ok("保存成功");
    }

    private Integer parseEnabled(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean bool) return bool ? 1 : 0;
        if (value instanceof Number number) return number.intValue() == 0 ? 0 : 1;

        String text = String.valueOf(value).trim();
        if (text.isEmpty()) return null;
        if ("true".equalsIgnoreCase(text)) return 1;
        if ("false".equalsIgnoreCase(text)) return 0;

        try {
            return Integer.parseInt(text) == 0 ? 0 : 1;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("enabled参数无效");
        }
    }
}
