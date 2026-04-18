package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.PopupConfig;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.PopupConfigService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/popup")
public class PopupConfigController {

    private final PopupConfigService service;
    private final SessionHelper sessionHelper;

    public PopupConfigController(PopupConfigService service, SessionHelper sessionHelper) {
        this.service = service;
        this.sessionHelper = sessionHelper;
    }

    /**
     * GET /api/popup/active
     * 获取所有激活的弹窗
     */
    @GetMapping("/active")
    public ApiResponse<List<PopupConfig>> getActivePopups() {
        List<PopupConfig> popups = service.getActivePopups();
        return ApiResponse.ok(popups);
    }

    /**
     * GET /api/popup/list
     * 获取所有弹窗（管理端）
     */
    @GetMapping("/list")
    public ApiResponse<List<PopupConfig>> list(HttpServletRequest request) {
        // 简单的管理员验证
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || !sessionId.startsWith("admin")) {
            return ApiResponse.fail("无权访问");
        }
        return ApiResponse.ok(service.getAll());
    }

    /**
     * POST /api/popup/save
     * 保存弹窗配置
     */
    @PostMapping("/save")
    public ApiResponse<PopupConfig> save(@RequestBody PopupConfig config, HttpServletRequest request) {
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || !sessionId.startsWith("admin")) {
            return ApiResponse.fail("无权访问");
        }
        service.save(config);
        return ApiResponse.ok(config);
    }

    /**
     * DELETE /api/popup/delete/{id}
     * 删除弹窗配置
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String sessionId = request.getHeader("X-Session-Id");
        if (sessionId == null || !sessionId.startsWith("admin")) {
            return ApiResponse.fail("无权访问");
        }
        service.delete(id);
        return ApiResponse.ok(null);
    }
}
