-- =====================================================
-- 拼豆V1.0 删除废弃表
-- Date: 2026-04-18
-- =====================================================

-- 旧任务记录表（已被 bp_box/bp_history/bp_draft 替代）
DROP TABLE IF EXISTS `bp_image_task`;

-- 创作者图纸表（V1.0去掉创作者中心）
DROP TABLE IF EXISTS `bp_creator_pattern`;

-- 图纸下载记录表（与创作者相关）
DROP TABLE IF EXISTS `bp_pattern_download`;

-- 提现申请表（V1.0不做会员功能）
DROP TABLE IF EXISTS `bp_withdraw`;

-- 短信验证码表（V1.0不做短信登录）
DROP TABLE IF EXISTS `bp_sms_code`;

-- 金币流水表（V1.0不做金币系统）
DROP TABLE IF EXISTS `bp_coin_log`;
