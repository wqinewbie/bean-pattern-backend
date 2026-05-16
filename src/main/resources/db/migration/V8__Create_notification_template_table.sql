-- 消息模板初始化数据（表 bp_notification_template 已存在，仅插入默认数据）
INSERT IGNORE INTO `bp_notification_template` (`code`, `name`, `type`, `title`, `content`, `icon`, `action_type`, `action_value`, `action_text`, `variables`, `is_active`) VALUES
('vip_expire', 'VIP到期提醒', 'vip_expire', 'VIP即将到期', '您的VIP会员将于 {expireDate} 到期，记得及时续费哦', '👑', 'PAGE', '/pages/vip/vip', '立即续费', '[{"key":"expireDate","desc":"到期日期"}]', 1),
('ai_quota_low', 'AI次数不足提醒', 'ai_low', 'AI魔法次数不足', '您的AI魔法次数仅剩 {remainingCount} 次，完成任务可继续领取', '🪄', 'PAGE', '/pages/profile/profile?action=task', '去完成任务', '[{"key":"remainingCount","desc":"剩余次数"}]', 1),
('gift_received', '礼品到账通知', 'gift', '礼品已到账', '恭喜您获得 {giftName}，快去查看吧', '🎁', 'GIFT', '{giftId}', '查看礼品', '[{"key":"giftName","desc":"礼品名称"},{"key":"giftId","desc":"礼品ID"}]', 1),
('system_notice', '系统通知', 'system', '系统通知', '{content}', '🔔', 'NONE', '', '', '[{"key":"content","desc":"通知内容"}]', 1);

-- 添加字典数据：消息类型
INSERT IGNORE INTO `bp_sys_dict_item` (`dict_type`, `dict_value`, `dict_label`, `tag_type`, `sort_order`, `status`, `disabled`) VALUES
('notification_type', 'system', '系统通知', 'info', 1, 1, 0),
('notification_type', 'vip_expire', 'VIP到期', 'warning', 2, 1, 0),
('notification_type', 'gift', '礼品通知', 'success', 3, 1, 0),
('notification_type', 'ai_low', 'AI次数不足', 'danger', 4, 1, 0),
('notification_type', 'activity', '活动通知', 'primary', 5, 1, 0);

-- 添加字典数据：操作类型
INSERT IGNORE INTO `bp_sys_dict_item` (`dict_type`, `dict_value`, `dict_label`, `tag_type`, `sort_order`, `status`, `disabled`) VALUES
('notification_action_type', 'NONE', '无操作', 'info', 1, 1, 0),
('notification_action_type', 'PAGE', '跳转页面', 'primary', 2, 1, 0),
('notification_action_type', 'URL', '打开链接', 'warning', 3, 1, 0),
('notification_action_type', 'GIFT', '查看礼品', 'success', 4, 1, 0);