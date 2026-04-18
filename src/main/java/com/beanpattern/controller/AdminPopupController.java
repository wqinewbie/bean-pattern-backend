package com.beanpattern.controller;

import com.beanpattern.entity.PopupConfig;
import com.beanpattern.mapper.AdminMapper;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.PopupConfigService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理 - 弹窗配置管理
 * 路径: /api/admin/popup/**
 */
@RestController
@RequestMapping("/api/admin/popup")
public class AdminPopupController {

    private final PopupConfigService service;
    private final AdminMapper adminMapper;

    public AdminPopupController(PopupConfigService service, AdminMapper adminMapper) {
        this.service = service;
        this.adminMapper = adminMapper;
    }

    /**
     * 验证管理员身份
     */
    private boolean verifyAdmin(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            return false;
        }
        // token = base64(username:timestamp:sign)
        try {
            String token = auth.substring(7);
            String decoded = new String(java.util.Base64.getDecoder().decode(token));
            String username = decoded.split(":")[0];
            var admin = adminMapper.findByUsername(username);
            return admin != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * GET /api/admin/popup/list
     * 获取所有弹窗
     */
    @GetMapping("/list")
    public ApiResponse<List<PopupConfig>> list(HttpServletRequest request) {
        if (!verifyAdmin(request)) {
            return ApiResponse.fail("无权访问");
        }
        return ApiResponse.ok(service.getAll());
    }

    /**
     * POST /api/admin/popup/save
     * 保存弹窗配置
     */
    @PostMapping("/save")
    public ApiResponse<PopupConfig> save(@RequestBody PopupConfig config, HttpServletRequest request) {
        if (!verifyAdmin(request)) {
            return ApiResponse.fail("无权访问");
        }
        service.save(config);
        return ApiResponse.ok(config);
    }

    /**
     * DELETE /api/admin/popup/delete/{id}
     * 删除弹窗配置
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        if (!verifyAdmin(request)) {
            return ApiResponse.fail("无权访问");
        }
        service.delete(id);
        return ApiResponse.ok(null);
    }
}
