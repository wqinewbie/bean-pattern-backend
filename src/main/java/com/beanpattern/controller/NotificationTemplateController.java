package com.beanpattern.controller;

import com.beanpattern.entity.NotificationTemplate;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/notification-templates")
@RequiredArgsConstructor
public class NotificationTemplateController {

    private final NotificationTemplateService notificationTemplateService;

    @GetMapping
    public ApiResponse<List<NotificationTemplate>> list() {
        return ApiResponse.ok(notificationTemplateService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationTemplate> getById(@PathVariable Long id) {
        return ApiResponse.ok(notificationTemplateService.getById(id));
    }

    @PostMapping
    public ApiResponse<NotificationTemplate> create(@RequestBody NotificationTemplate template) {
        return ApiResponse.ok(notificationTemplateService.create(template));
    }

    @PutMapping("/{id}")
    public ApiResponse<NotificationTemplate> update(@PathVariable Long id, @RequestBody NotificationTemplate template) {
        template.setId(id);
        return ApiResponse.ok(notificationTemplateService.update(template));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Boolean isActive) {
        notificationTemplateService.updateStatus(id, isActive);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        notificationTemplateService.delete(id);
        return ApiResponse.ok(null);
    }
}
