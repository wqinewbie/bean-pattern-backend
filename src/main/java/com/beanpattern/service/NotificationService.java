package com.beanpattern.service;

import com.beanpattern.entity.NotificationTemplate;
import com.beanpattern.entity.UserNotification;
import com.beanpattern.mapper.UserNotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知服务
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserNotificationMapper notificationMapper;
    private final NotificationTemplateService notificationTemplateService;

    /**
     * 获取用户通知列表
     */
    public List<UserNotification> getUserNotifications(Long userId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 50; // 默认最多返回50条
        }
        return notificationMapper.findByUserId(userId, limit);
    }

    /**
     * 获取用户未读通知数量
     */
    public int getUnreadCount(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public boolean markAsRead(Long userId, Long notificationId) {
        UserNotification notification = notificationMapper.findById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }
        return notificationMapper.markAsRead(notificationId) > 0;
    }

    /**
     * 标记所有通知为已读
     */
    @Transactional
    public boolean markAllAsRead(Long userId) {
        return notificationMapper.markAllAsRead(userId) >= 0;
    }

    /**
     * 删除通知
     */
    @Transactional
    public boolean deleteNotification(Long userId, Long notificationId) {
        return notificationMapper.deleteById(notificationId, userId) > 0;
    }

    /**
     * 创建通知
     */
    @Transactional
    public UserNotification createNotification(UserNotification notification) {
        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 创建系统通知（简化方法）
     */
    @Transactional
    public UserNotification createSystemNotification(Long userId, String title, String content) {
        NotificationTemplate template = notificationTemplateService.getByCode("system_notice");
        if (template != null && template.getIsActive()) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("content", content);
            return createNotificationFromTemplate(userId, template, variables);
        }

        // 降级：使用硬编码
        UserNotification notification = UserNotification.builder()
                .userId(userId)
                .type("system")
                .title(title)
                .content(content)
                .icon("🔔")
                .actionType("NONE")
                .isRead(false)
                .build();
        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 创建礼品通知
     */
    @Transactional
    public UserNotification createGiftNotification(Long userId, String title, String content, Long giftId) {
        NotificationTemplate template = notificationTemplateService.getByCode("gift_received");
        if (template != null && template.getIsActive()) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("giftName", title);
            variables.put("giftId", giftId);

            return createNotificationFromTemplate(userId, template, variables, "gift", giftId);
        }

        // 降级：使用硬编码
        UserNotification notification = UserNotification.builder()
                .userId(userId)
                .type("gift")
                .title(title)
                .content(content)
                .icon("🎁")
                .actionType("GIFT")
                .actionValue(String.valueOf(giftId))
                .actionText("查看礼品")
                .relatedType("gift")
                .relatedId(giftId)
                .isRead(false)
                .build();
        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 创建VIP到期提醒
     */
    @Transactional
    public UserNotification createVipExpireNotification(Long userId, String expireDate) {
        NotificationTemplate template = notificationTemplateService.getByCode("vip_expire");
        if (template != null && template.getIsActive()) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("expireDate", expireDate);
            return createNotificationFromTemplate(userId, template, variables);
        }

        // 降级：使用硬编码
        UserNotification notification = UserNotification.builder()
                .userId(userId)
                .type("vip_expire")
                .title("VIP即将到期")
                .content("您的VIP会员将于 " + expireDate + " 到期，记得及时续费哦")
                .icon("👑")
                .actionType("PAGE")
                .actionValue("/pages/vip/vip")
                .actionText("立即续费")
                .isRead(false)
                .build();
        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 创建VIP续费/开通通知
     */
    @Transactional
    public UserNotification createVipRenewNotification(Long userId, String expireDate) {
        NotificationTemplate template = notificationTemplateService.getByCode("vip_renew");
        if (template != null && template.getIsActive()) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("expireDate", expireDate);
            return createNotificationFromTemplate(userId, template, variables);
        }

        // 降级：使用硬编码
        UserNotification notification = UserNotification.builder()
                .userId(userId)
                .type("vip_renew")
                .title("VIP开通成功")
                .content("恭喜！您的VIP会员已开通，有效期至 " + expireDate)
                .icon("👑")
                .actionType("PAGE")
                .actionValue("/pages/vip/vip")
                .actionText("查看权益")
                .isRead(false)
                .build();
        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 创建AI魔法次数提醒
     */
    @Transactional
    public UserNotification createAiQuotaNotification(Long userId, int remainingCount) {
        NotificationTemplate template = notificationTemplateService.getByCode("ai_quota_low");
        if (template != null && template.getIsActive()) {
            Map<String, Object> variables = new HashMap<>();
            variables.put("remainingCount", remainingCount);
            return createNotificationFromTemplate(userId, template, variables);
        }

        // 降级：使用硬编码
        UserNotification notification = UserNotification.builder()
                .userId(userId)
                .type("ai_low")
                .title("AI魔法次数不足")
                .content("您的AI魔法次数仅剩 " + remainingCount + " 次，完成任务可继续领取")
                .icon("🪄")
                .actionType("PAGE")
                .actionValue("/pages/profile/profile?action=task")
                .actionText("去完成任务")
                .isRead(false)
                .build();
        notificationMapper.insert(notification);
        return notification;
    }

    /**
     * 从模板创建通知（通用方法）
     */
    private UserNotification createNotificationFromTemplate(Long userId, NotificationTemplate template, Map<String, Object> variables) {
        return createNotificationFromTemplate(userId, template, variables, null, null);
    }

    /**
     * 从模板创建通知（通用方法，支持关联数据）
     */
    private UserNotification createNotificationFromTemplate(Long userId, NotificationTemplate template,
                                                           Map<String, Object> variables,
                                                           String relatedType, Long relatedId) {
        String title = notificationTemplateService.renderTemplate(template.getTitle(), variables);
        String content = notificationTemplateService.renderTemplate(template.getContent(), variables);
        String actionValue = template.getActionValue() != null ?
                notificationTemplateService.renderTemplate(template.getActionValue(), variables) : null;

        UserNotification notification = UserNotification.builder()
                .userId(userId)
                .type(template.getType())
                .templateCode(template.getCode())
                .title(title)
                .content(content)
                .icon(template.getIcon())
                .actionType(template.getActionType())
                .actionValue(actionValue)
                .actionText(template.getActionText())
                .relatedType(relatedType)
                .relatedId(relatedId)
                .isRead(false)
                .build();
        notificationMapper.insert(notification);
        return notification;
    }
}
