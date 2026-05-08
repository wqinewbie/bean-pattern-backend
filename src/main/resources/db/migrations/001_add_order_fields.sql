-- 订单表字段补充
-- 日期: 2026-05-08
-- 说明: 添加订单表缺失的字段，修复 OrderMapper SQL 报错

ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `product_id` BIGINT NULL COMMENT '商品ID' AFTER `plan_name`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `vip_level_purchased` TINYINT(1) NULL COMMENT '购买的VIP等级' AFTER `product_id`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `vip_days` INT NULL COMMENT 'VIP天数' AFTER `vip_level_purchased`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `gift_items` TEXT NULL COMMENT '礼品项JSON' AFTER `vip_days`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `product_type` VARCHAR(32) NULL COMMENT '商品类型：vip/card/gift' AFTER `gift_items`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `package_code` VARCHAR(64) NULL COMMENT '套餐代码' AFTER `product_type`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `expire_at` DATETIME NULL COMMENT '订单过期时间' AFTER `package_code`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `deliver_status` VARCHAR(32) NULL DEFAULT 'PENDING' COMMENT '发货状态：PENDING/SUCCESS/FAILED' AFTER `expire_at`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `deliver_error` VARCHAR(512) NULL COMMENT '发货错误信息' AFTER `deliver_status`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `transaction_id` VARCHAR(64) NULL COMMENT '微信交易单号' AFTER `deliver_error`;
