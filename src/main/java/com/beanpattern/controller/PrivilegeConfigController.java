package com.beanpattern.controller;

import com.beanpattern.entity.PrivilegeConfig;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.PrivilegeConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权益配置管理接口（管理后台）
 */
@RestController
@RequestMapping("/api/admin/privileges")
public class PrivilegeConfigController {

    private final PrivilegeConfigService privilegeConfigService;

    public PrivilegeConfigController(PrivilegeConfigService privilegeConfigService) {
        this.privilegeConfigService = privilegeConfigService;
    }

    /**
     * 获取所有权益配置
     */
    @GetMapping
    public ApiResponse<List<PrivilegeConfig>> list() {
        try {
            List<PrivilegeConfig> configs = privilegeConfigService.listAllConfigs();
            return ApiResponse.ok(configs);
        } catch (Exception e) {
            return ApiResponse.fail("获取权益配置列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取单个权益配置
     */
    @GetMapping("/{id}")
    public ApiResponse<PrivilegeConfig> getById(@PathVariable Long id) {
        try {
            PrivilegeConfig config = privilegeConfigService.getById(id);
            if (config == null) {
                return ApiResponse.fail("权益配置不存在");
            }
            return ApiResponse.ok(config);
        } catch (Exception e) {
            return ApiResponse.fail("获取权益配置失败: " + e.getMessage());
        }
    }

    /**
     * 更新权益配置
     */
    @PutMapping("/{id}")
    public ApiResponse<PrivilegeConfig> update(@PathVariable Long id, @RequestBody PrivilegeConfig privilegeConfig) {
        try {
            privilegeConfig.setId(id);
            PrivilegeConfig updated = privilegeConfigService.update(privilegeConfig);
            return ApiResponse.ok(updated);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("更新权益配置失败: " + e.getMessage());
        }
    }
}
