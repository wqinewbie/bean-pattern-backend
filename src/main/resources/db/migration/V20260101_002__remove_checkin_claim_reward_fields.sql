-- 删除签到领取记录表的冗余奖励字段
-- 奖励信息应从礼品包动态获取，不应在记录表中冗余存储

ALTER TABLE bp_user_checkin_claim DROP COLUMN reward_type;
ALTER TABLE bp_user_checkin_claim DROP COLUMN reward_value;
