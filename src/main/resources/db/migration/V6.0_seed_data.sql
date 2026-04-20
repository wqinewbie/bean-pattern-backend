-- ============================================
-- V6.0 种子数据初始化
-- 包含VIP产品、任务配置、礼品类型等
-- ============================================

-- 1. VIP产品初始化 (vip_product)
INSERT INTO vip_product (product_code, name, description, vip_level, ai_quota_per_month, storage_quota, draft_quota, available_brands, color_limit_per_brand, price, original_price, wx_product_id, valid_days, sort_order, status, created_at, updated_at) VALUES
-- 试用会员 (Level 1)
('trial_7d', '7天试用会员', '7天内免费试用VIP功能', 1, 10, 50, 20, 'perler,hama', 5, 0.00, 9.90, NULL, 7, 1, 1, NOW(), NOW()),
-- 基础会员 (Level 2) 
('basic_30d', '基础月卡', '30天基础VIP会员，享受更多功能', 2, 30, 200, 100, 'perler,hama,artkal', 10, 19.90, 29.90, NULL, 30, 2, 1, NOW(), NOW()),
-- 高级会员 (Level 3)
('premium_90d', '高级季卡', '90天高级VIP会员，更多配额', 3, 100, 500, 300, 'perler,hama,artkal,nabbi', 15, 49.90, 79.90, NULL, 90, 3, 1, NOW(), NOW()),
-- 至尊会员 (Level 4)
('ultimate_365d', '至尊年卡', '365天至尊VIP会员，全功能解锁', 4, 500, 2000, 1000, 'perler,hama,artkal,nabbi,mininail', 20, 199.00, 399.00, NULL, 365, 4, 1, NOW(), NOW());

-- 2. 任务配置初始化 (task_config)
INSERT INTO task_config (task_code, task_name, task_type, description, target_count, reward_type, reward_value, icon_url, action_url, sort_order, status, created_at, updated_at) VALUES
-- 每日任务
('daily_login', '每日登录', 'DAILY', '每天登录应用', 1, 'AI_COUNT', 2, NULL, NULL, 1, 1, NOW(), NOW()),
('daily_share', '每日分享', 'DAILY', '分享作品给好友', 1, 'AI_COUNT', 3, NULL, NULL, 2, 1, NOW(), NOW()),
('daily_create', '每日创作', 'DAILY', '每天生成或绘制一幅作品', 1, 'AI_COUNT', 5, NULL, NULL, 3, 1, NOW(), NOW()),
('daily_use_box', '使用图纸', 'DAILY', '使用图纸箱中的作品', 1, 'AI_COUNT', 2, NULL, NULL, 4, 1, NOW(), NOW()),

-- 每周任务
('weekly_create_5', '每周创作达人', 'WEEKLY', '每周完成5次创作', 5, 'AI_COUNT', 20, NULL, NULL, 10, 1, NOW(), NOW()),
('weekly_share_3', '每周分享达人', 'WEEKLY', '每周分享3次作品', 3, 'AI_COUNT', 10, NULL, NULL, 11, 1, NOW(), NOW()),
('weekly_invite_2', '每周邀请好友', 'WEEKLY', '每周成功邀请2位好友', 2, 'AI_COUNT', 15, NULL, NULL, 12, 1, NOW(), NOW()),

-- 一次性任务
('first_login', '首次登录', 'ONCE', '完成首次登录', 1, 'AI_COUNT', 10, NULL, NULL, 20, 1, NOW(), NOW()),
('first_create', '首次创作', 'ONCE', '完成首次作品生成', 1, 'AI_COUNT', 5, NULL, NULL, 21, 1, NOW(), NOW()),
('first_share', '首次分享', 'ONCE', '首次分享作品', 1, 'AI_COUNT', 3, NULL, NULL, 22, 1, NOW(), NOW()),
('bind_phone', '绑定手机', 'ONCE', '绑定手机号码', 1, 'AI_COUNT', 5, NULL, NULL, 23, 1, NOW(), NOW()),
('complete_1_box', '完成首幅作品', 'ONCE', '完成第一幅拼豆作品', 1, 'AI_COUNT', 10, NULL, NULL, 24, 1, NOW(), NOW());

-- 3. 礼品类型初始化 (gift_type)
INSERT INTO gift_type (code, name, gift_category, description, icon_url, sort_order, status, created_at, updated_at) VALUES
('vip_gift', 'VIP赠送', 'VIP_DAYS', '延长VIP会员时间', NULL, 1, 1, NOW(), NOW()),
('ai_gift', 'AI次数', 'AI_COUNT', '增加AI生成次数', NULL, 2, 1, NOW(), NOW()),
('coupon', '优惠券', 'COUPON', '满减优惠券', NULL, 3, 1, NOW(), NOW()),
('beads_kit', '拼豆材料包', 'BEADS_KIT', '拼豆材料包赠品', NULL, 4, 1, NOW(), NOW());

-- 4. 礼品项初始化 (gift_item)
INSERT INTO gift_item (gift_type_id, gift_code, name, value, total_quantity, remain_quantity, start_at, end_at, sort_order, status, created_at, updated_at) VALUES
-- VIP赠送
(1, 'vip_1d', 'VIP体验1天', 1, 1000, 1000, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 1, 1, NOW(), NOW()),
(1, 'vip_3d', 'VIP体验3天', 3, 500, 500, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 2, 1, NOW(), NOW()),
(1, 'vip_7d', 'VIP周卡', 7, 200, 200, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 3, 1, NOW(), NOW()),

-- AI次数
(2, 'ai_3', 'AI生成3次', 3, 1000, 1000, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 1, 1, NOW(), NOW()),
(2, 'ai_5', 'AI生成5次', 5, 500, 500, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 2, 1, NOW(), NOW()),
(2, 'ai_10', 'AI生成10次', 10, 200, 200, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 3, 1, NOW(), NOW()),

-- 优惠券
(3, 'coupon_5', '满29减5券', 5, 500, 500, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 1, 1, NOW(), NOW()),
(3, 'coupon_10', '满49减10券', 10, 300, 300, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 2, 1, NOW(), NOW()),
(3, 'coupon_20', '满99减20券', 20, 100, 100, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR), 3, 1, NOW(), NOW());

-- 5. 初始化系统默认用户配额（如需）
-- 为已有用户补充V6.0新增字段的默认值
-- UPDATE bp_user SET storage_quota = 10, draft_quota = 5 WHERE storage_quota IS NULL;
