-- 订单表清理为新版会员/次卡订单结构
-- 日期: 2026-05-08
-- 说明: 移除旧充值套餐流程字段，仅保留 product_type + package_code 商品识别方式

DROP TABLE IF EXISTS `bp_recharge_plan`;

ALTER TABLE `bp_order` DROP COLUMN IF EXISTS `plan_id`;
ALTER TABLE `bp_order` DROP COLUMN IF EXISTS `product_id`;
ALTER TABLE `bp_order` DROP COLUMN IF EXISTS `vip_level_purchased`;
ALTER TABLE `bp_order` DROP COLUMN IF EXISTS `vip_days`;
ALTER TABLE `bp_order` DROP COLUMN IF EXISTS `gift_items`;
ALTER TABLE `bp_order` DROP COLUMN IF EXISTS `coins`;
ALTER TABLE `bp_order` DROP COLUMN IF EXISTS `ai_quota`;
ALTER TABLE `bp_order` DROP COLUMN IF EXISTS `pay_type`;
ALTER TABLE `bp_order` DROP COLUMN IF EXISTS `wx_transaction_id`;

ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `product_type` VARCHAR(32) NULL COMMENT '商品类型：vip/card/gift' AFTER `user_id`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `package_code` VARCHAR(64) NULL COMMENT '套餐代码' AFTER `product_type`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `expire_at` DATETIME NULL COMMENT '订单过期时间' AFTER `status`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `deliver_status` VARCHAR(32) NULL DEFAULT 'PENDING' COMMENT '发货状态：PENDING/SUCCESS/FAILED' AFTER `expire_at`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `deliver_error` VARCHAR(512) NULL COMMENT '发货错误信息' AFTER `deliver_status`;
ALTER TABLE `bp_order` ADD COLUMN IF NOT EXISTS `transaction_id` VARCHAR(64) NULL COMMENT '支付交易单号' AFTER `deliver_error`;

ALTER TABLE `bp_order` MODIFY COLUMN `product_type` VARCHAR(32) NOT NULL COMMENT '商品类型：vip/card/gift';
ALTER TABLE `bp_order` MODIFY COLUMN `package_code` VARCHAR(64) NOT NULL COMMENT '套餐代码';
ALTER TABLE `bp_order` MODIFY COLUMN `plan_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '下单时套餐名称快照';
ALTER TABLE `bp_order` MODIFY COLUMN `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/REFUNDED/CANCELLED/TIMEOUT';

ALTER TABLE `bp_order` ADD INDEX IF NOT EXISTS `idx_product_package` (`product_type`, `package_code`);
