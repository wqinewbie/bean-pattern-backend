-- 活动中心表结构补充
-- 日期: 2026-05-14
-- 说明: 补齐 ActivityConfigMapper / UserActivityLogMapper 依赖的表和字段，修复 bp_activity_config 缺少 cover_image 等字段导致的 SQL 报错

CREATE TABLE IF NOT EXISTS `bp_activity_config` (
  `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `activity_code`   VARCHAR(64) NOT NULL COMMENT '活动编码',
  `title`           VARCHAR(128) NOT NULL COMMENT '活动标题',
  `description`     VARCHAR(512) NULL COMMENT '活动描述',
  `cover_image`     VARCHAR(1024) NULL COMMENT '封面图片URL',
  `banner_id`       BIGINT NULL COMMENT '关联Banner ID',
  `activity_type`   VARCHAR(32) NOT NULL DEFAULT 'CONTENT' COMMENT '活动类型：CONTENT/GIFT',
  `gift_package_code` VARCHAR(64) NULL COMMENT '活动绑定礼品包编码',
  `limit_type`      VARCHAR(32) NOT NULL DEFAULT 'ONCE' COMMENT '限制类型：ONCE/DAILY/UNLIMITED',
  `total_quota`     INT NOT NULL DEFAULT 0 COMMENT '总名额，0表示不限量',
  `remain_quota`    INT NOT NULL DEFAULT 0 COMMENT '剩余名额',
  `start_at`        DATETIME NOT NULL COMMENT '活动开始时间',
  `end_at`          DATETIME NOT NULL COMMENT '活动结束时间',
  `content_html`    MEDIUMTEXT NULL COMMENT '富文本HTML内容',
  `content_json`    JSON NULL COMMENT '富文本JSON结构',
  `page_type`       VARCHAR(32) NOT NULL DEFAULT 'RICH_TEXT' COMMENT '页面类型：RICH_TEXT/CUSTOM',
  `button_text`     VARCHAR(64) NULL COMMENT '按钮文案',
  `button_action`   VARCHAR(32) NULL COMMENT '按钮动作：CLAIM/RECHARGE/JUMP',
  `button_url`      VARCHAR(512) NULL COMMENT '按钮跳转URL',
  `status`          TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0下线 1上线',
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_code` (`activity_code`),
  KEY `idx_status_time` (`status`, `start_at`, `end_at`),
  KEY `idx_banner_id` (`banner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动配置表';

ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `cover_image` VARCHAR(1024) NULL COMMENT '封面图片URL' AFTER `description`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `banner_id` BIGINT NULL COMMENT '关联Banner ID' AFTER `cover_image`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `activity_type` VARCHAR(32) NOT NULL DEFAULT 'CONTENT' COMMENT '活动类型：CONTENT/GIFT' AFTER `banner_id`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `gift_package_code` VARCHAR(64) NULL COMMENT '活动绑定礼品包编码' AFTER `activity_type`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `limit_type` VARCHAR(32) NOT NULL DEFAULT 'ONCE' COMMENT '限制类型：ONCE/DAILY/UNLIMITED' AFTER `gift_package_code`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `total_quota` INT NOT NULL DEFAULT 0 COMMENT '总名额，0表示不限量' AFTER `limit_type`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `remain_quota` INT NOT NULL DEFAULT 0 COMMENT '剩余名额' AFTER `total_quota`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `start_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '活动开始时间' AFTER `remain_quota`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `end_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '活动结束时间' AFTER `start_at`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `content_html` MEDIUMTEXT NULL COMMENT '富文本HTML内容' AFTER `end_at`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `content_json` JSON NULL COMMENT '富文本JSON结构' AFTER `content_html`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `page_type` VARCHAR(32) NOT NULL DEFAULT 'RICH_TEXT' COMMENT '页面类型：RICH_TEXT/CUSTOM' AFTER `content_json`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `button_text` VARCHAR(64) NULL COMMENT '按钮文案' AFTER `page_type`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `button_action` VARCHAR(32) NULL COMMENT '按钮动作：CLAIM/RECHARGE/JUMP' AFTER `button_text`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `button_url` VARCHAR(512) NULL COMMENT '按钮跳转URL' AFTER `button_action`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0下线 1上线' AFTER `button_url`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `status`;
ALTER TABLE `bp_activity_config` ADD COLUMN IF NOT EXISTS `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`;

CREATE TABLE IF NOT EXISTS `bp_user_activity_log` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT NOT NULL COMMENT '用户ID',
  `activity_id`   BIGINT NOT NULL COMMENT '活动ID',
  `activity_code` VARCHAR(64) NOT NULL COMMENT '活动编码',
  `action_type`   VARCHAR(32) NOT NULL COMMENT '操作类型：VIEW/CLAIM/COMPLETE',
  `reward_type`   VARCHAR(32) NULL COMMENT '奖励类型：GIFT/COINS/AI_QUOTA/VIP_DAYS',
  `reward_value`  INT NULL COMMENT '奖励值',
  `gift_id`       BIGINT NULL COMMENT '礼品ID',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `created_at`),
  KEY `idx_activity_time` (`activity_id`, `created_at`),
  KEY `idx_activity_code` (`activity_code`),
  KEY `idx_user_activity_action` (`user_id`, `activity_id`, `action_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户活动参与记录表';
