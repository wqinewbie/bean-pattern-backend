-- 审核任务结果通知模板和消息类型
INSERT IGNORE INTO `bp_notification_template`
(`code`, `name`, `type`, `title`, `content`, `icon`, `action_type`, `action_value`, `action_text`, `variables`, `is_active`)
VALUES
('review_task_result', '审核任务结果通知', 'review_task', '任务审核结果通知',
 '您的任务 {taskCode} 审核结果：{result}。{remark}', '📣', 'NONE', '', '',
 '[{"key":"taskCode","desc":"任务编码"},{"key":"result","desc":"审核结果"},{"key":"remark","desc":"审核备注"}]', 1);

INSERT IGNORE INTO `bp_sys_dict_item`
(`dict_type`, `dict_value`, `dict_label`, `tag_type`, `sort_order`, `status`, `disabled`, `remark`)
VALUES
('notification_type', 'review_task', '审核任务通知', 'warning', 7, 1, 0, '');
