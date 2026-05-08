package com.beanpattern.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户消息通知表实体：bp_user_notification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserNotification {
    private Long id;
    private Long userId;               // 用户ID
    private String type;               // 消息类型：vip_expire/vip_renew/ai_low/gift/activity/system
    private String templateCode;       // 消息模板编码
    private String title;              // 消息标题
    private String content;            // 消息内容
    private String icon;               // 消息图标
    private String actionType;         // 操作类型：NONE/PAGE/URL/GIFT
    private String actionValue;        // 操作值（页面路径/URL/礼品ID）
    private String actionText;         // 操作按钮文案
    private Boolean isRead;            // 是否已读
    private String relatedType;        // 关联类型：order/gift/activity
    private Long relatedId;            // 关联ID
    private String extraData;          // 扩展数据（JSON）
    private LocalDateTime createdAt;   // 创建时间
    private LocalDateTime updatedAt;   // 更新时间
}
