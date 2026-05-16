-- 删除 TaskConfig 表中的冗余奖励字段
-- 这些字段的信息已经通过 GiftPackage 的 itemsJson 动态获取

ALTER TABLE bp_task_config DROP COLUMN IF EXISTS reward_type;
ALTER TABLE bp_task_config DROP COLUMN IF EXISTS reward_value;
