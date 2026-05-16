package com.beanpattern.controller;

import com.beanpattern.entity.NotificationTemplate;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 消息模板配置管理接口
 */
@RestController
@RequestMapping("/api/admin/notification-templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    /**
     * 获取所有模板列表
     */
    @GetMapping
    public ApiResponse<List<NotificationTemplate>> list() {
        try {
            List<NotificationTemplate> templates = notificationTemplateService.listAll();
            return ApiResponse.ok(templates);
        } catch (Exception e) {
            return ApiResponse.fail("获取模板列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取单个模板
     */
    @GetMapping("/{id}")
    public ApiResponse<NotificationTemplate> getById(@PathVariable Long id) {
        try {
            NotificationTemplate template = notificationTemplateService.getById(id);
            return ApiResponse.ok(template);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("获取模板失败：" + e.getMessage());
        }
    }

    /**
     * 创建模板
     */
    @PostMapping
    public ApiResponse<NotificationTemplate> create(@RequestBody NotificationTemplate template) {
        try {
            NotificationTemplate created = notificationTemplateService.create(template);
            return ApiResponse.ok(created);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("创建模板失败：" + e.getMessage());
        }
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public ApiResponse<NotificationTemplate> update(@PathVariable Long id, @RequestBody NotificationTemplate template) {
        try {
            template.setId(id);
            NotificationTemplate updated = notificationTemplateService.update(template);
            return ApiResponse.ok(updated);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("更新模板失败：" + e.getMessage());
        }
    }

    /**
     * 更新模板状态（启用/禁用）
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        try {
            notificationTemplateService.updateStatus(id, isActive);
            return ApiResponse.ok(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("更新状态失败：" + e.getMessage());
        }
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            notificationTemplateService.delete(id);
            return ApiResponse.ok(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.fail("删除模板失败：" + e.getMessage());
        }
    }
}
