-- 消息模板与用户站内消息表兜底建表
CREATE TABLE IF NOT EXISTS `bp_notification_template` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL COMMENT '模板编码',
  `name` VARCHAR(128) NOT NULL COMMENT '模板名称',
  `type` VARCHAR(32) NOT NULL COMMENT '消息类型',
  `title` VARCHAR(128) NOT NULL COMMENT '标题模板',
  `content` VARCHAR(1024) NOT NULL COMMENT '内容模板',
  `icon` VARCHAR(32) NULL COMMENT '图标',
  `action_type` VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/PAGE/URL/GIFT',
  `action_value` VARCHAR(512) NULL COMMENT '页面路径/URL/礼品ID',
  `action_text` VARCHAR(64) NULL COMMENT '操作按钮文案',
  `variables` VARCHAR(1024) NULL COMMENT '变量说明JSON',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_notification_template_code` (`code`),
  KEY `idx_notification_template_active` (`is_active`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息模板配置表';

CREATE TABLE IF NOT EXISTS `bp_user_notification` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` VARCHAR(32) NOT NULL COMMENT '消息类型',
  `template_code` VARCHAR(64) NULL COMMENT '模板编码',
  `title` VARCHAR(128) NOT NULL COMMENT '消息标题',
  `content` VARCHAR(1024) NOT NULL COMMENT '消息内容',
  `icon` VARCHAR(32) NULL COMMENT '图标',
  `action_type` VARCHAR(32) NOT NULL DEFAULT 'NONE' COMMENT 'NONE/PAGE/URL/GIFT',
  `action_value` VARCHAR(512) NULL COMMENT '页面路径/URL/礼品ID',
  `action_text` VARCHAR(64) NULL COMMENT '操作按钮文案',
  `is_read` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `related_type` VARCHAR(32) NULL COMMENT '关联类型',
  `related_id` BIGINT NULL COMMENT '关联ID',
  `extra_data` TEXT NULL COMMENT '扩展数据JSON',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_user_notification_user_time` (`user_id`, `created_at`),
  KEY `idx_user_notification_unread` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户站内消息表';
