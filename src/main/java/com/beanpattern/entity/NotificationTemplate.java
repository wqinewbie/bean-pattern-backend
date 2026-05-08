package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息模板表实体：bp_notification_template
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {
    private Long id;
    private String code;               // 模板编码
    private String name;               // 模板名称
    private String type;               // 消息类型
    private String title;              // 标题模板（支持变量）
    private String content;            // 内容模板（支持变量）
    private String icon;               // 图标
    private String actionType;         // 操作类型
    private String actionValue;        // 操作值
    private String actionText;         // 操作按钮文案
    private String variables;          // 变量说明（JSON）
    private Boolean isActive;          // 是否启用
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间
}
