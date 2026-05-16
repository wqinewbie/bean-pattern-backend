-- 新增VIP续费/开通通知模板
INSERT IGNORE INTO `bp_notification_template` (`code`, `name`, `type`, `title`, `content`, `icon`, `action_type`, `action_value`, `action_text`, `variables`, `is_active`) VALUES
('vip_renew', 'VIP续费/开通通知', 'vip_renew', 'VIP开通成功', '恭喜！您的VIP会员已开通，有效期至 {expireDate}', '👑', 'PAGE', '/pages/vip/vip', '查看权益', '[{"key":"expireDate","desc":"到期日期"}]', 1);

-- 补充字典数据：VIP续费通知类型
INSERT IGNORE INTO `bp_sys_dict_item` (`dict_type`, `dict_value`, `dict_label`, `tag_type`, `sort_order`, `status`, `disabled`) VALUES
('notification_type', 'vip_renew', 'VIP续费', 'success', 6, 1, 0);
