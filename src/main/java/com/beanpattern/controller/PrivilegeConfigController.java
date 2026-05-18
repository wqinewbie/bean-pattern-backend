package com.beanpattern.controller;

import com.beanpattern.entity.PrivilegeConfig;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.PrivilegeConfigService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/privileges")
public class PrivilegeConfigController {

    private final PrivilegeConfigService privilegeConfigService;

    public PrivilegeConfigController(PrivilegeConfigService privilegeConfigService) {
        this.privilegeConfigService = privilegeConfigService;
    }

    @GetMapping
    public ApiResponse<List<PrivilegeConfig>> list() {
        return ApiResponse.ok(privilegeConfigService.listAllConfigs());
    }

    @GetMapping("/{id}")
    public ApiResponse<PrivilegeConfig> getById(@PathVariable Long id) {
        PrivilegeConfig config = privilegeConfigService.getById(id);
        if (config == null) {
            return ApiResponse.fail("权益配置不存在");
        }
        return ApiResponse.ok(config);
    }

    @PutMapping("/{id}")
    public ApiResponse<PrivilegeConfig> update(@PathVariable Long id, @RequestBody PrivilegeConfig privilegeConfig) {
        privilegeConfig.setId(id);
        return ApiResponse.ok(privilegeConfigService.update(privilegeConfig));
    }
}
