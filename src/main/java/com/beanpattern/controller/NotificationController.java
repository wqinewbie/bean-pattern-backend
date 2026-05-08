package com.beanpattern.controller;

import com.beanpattern.config.SessionHelper;
import com.beanpattern.entity.UserEntity;
import com.beanpattern.entity.UserNotification;
import com.beanpattern.model.ApiResponse;
import com.beanpattern.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final SessionHelper sessionHelper;
    private final NotificationService notificationService;

    public NotificationController(SessionHelper sessionHelper,
                                   NotificationService notificationService) {
        this.sessionHelper = sessionHelper;
        this.notificationService = notificationService;
    }

    /**
     * 获取通知列表
     */
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> getNotifications(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "50") Integer limit) {
        UserEntity user = sessionHelper.requireUser(request);
        List<UserNotification> notifications = notificationService.getUserNotifications(user.getId(), limit);

        List<Map<String, Object>> result = notifications.stream().map(n -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", n.getId());
            map.put("type", n.getType());
            map.put("title", n.getTitle());
            map.put("content", n.getContent());
            map.put("icon", n.getIcon());
            map.put("read", n.getIsRead());
            map.put("time", formatTime(n.getCreatedAt()));

            // 操作相关
            if (n.getActionType() != null && !"NONE".equals(n.getActionType())) {
                map.put("actionType", n.getActionType());
                map.put("actionValue", n.getActionValue());
                map.put("actionText", n.getActionText());
            }

            return map;
        }).collect(Collectors.toList());

        return ApiResponse.ok(result);
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Object>> getUnreadCount(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        int count = notificationService.getUnreadCount(user.getId());
        return ApiResponse.ok(Map.of("count", count));
    }

    /**
     * 标记单条通知为已读
     */
    @PostMapping("/mark-read/{id}")
    public ApiResponse<Void> markAsRead(
            HttpServletRequest request,
            @PathVariable Long id) {
        UserEntity user = sessionHelper.requireUser(request);
        boolean success = notificationService.markAsRead(user.getId(), id);
        if (!success) {
            return ApiResponse.fail("通知不存在或无权限");
        }
        return ApiResponse.ok(null);
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/mark-all-read")
    public ApiResponse<Void> markAllAsRead(HttpServletRequest request) {
        UserEntity user = sessionHelper.requireUser(request);
        notificationService.markAllAsRead(user.getId());
        return ApiResponse.ok(null);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteNotification(
            HttpServletRequest request,
            @PathVariable Long id) {
        UserEntity user = sessionHelper.requireUser(request);
        boolean success = notificationService.deleteNotification(user.getId(), id);
        if (!success) {
            return ApiResponse.fail("通知不存在或无权限");
        }
        return ApiResponse.ok(null);
    }

    /**
     * 格式化时间显示
     */
    private String formatTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();

        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 1440) { // 24小时
            return (minutes / 60) + "小时前";
        } else if (dateTime.toLocalDate().equals(now.toLocalDate().minusDays(1))) {
            return "昨天";
        } else if (minutes < 10080) { // 7天
            return (minutes / 1440) + "天前";
        } else {
            return dateTime.format(DateTimeFormatter.ofPattern("MM-dd"));
        }
    }
}
