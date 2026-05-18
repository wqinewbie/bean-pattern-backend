package com.beanpattern.controller;

import com.beanpattern.entity.PopupConfig;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.PopupConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/popup")
public class PopupConfigController {

    private final PopupConfigService service;

    public PopupConfigController(PopupConfigService service) {
        this.service = service;
    }

    /**
     * GET /api/popup/active
     * 获取所有激活的弹窗（小程序公开接口）
     */
    @GetMapping("/active")
    public ApiResponse<List<PopupConfig>> getActivePopups() {
        List<PopupConfig> popups = service.getActivePopups();
        return ApiResponse.ok(popups);
    }
}
