-- V10: 创建生产库缺失的 bp_creator_pattern 表
-- 修复 P0-05 / P1-17：CreatorPatternMapper 运行时崩溃

CREATE TABLE IF NOT EXISTS `bp_creator_pattern` (
  `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT NOT NULL,
  `title`          VARCHAR(128) NOT NULL,
  `description`    VARCHAR(512) NULL,
  `cover_url`      VARCHAR(1024) NOT NULL,
  `pattern_url`    VARCHAR(1024) NOT NULL,
  `grid_size`      VARCHAR(32) NULL,
  `difficulty`     TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1简单 2中等 3困难',
  `price_coins`    INT NOT NULL DEFAULT 0,
  `download_count` INT NOT NULL DEFAULT 0,
  `like_count`     INT NOT NULL DEFAULT 0,
  `income_coins`   INT NOT NULL DEFAULT 0,
  `status`         TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0审核中 1上线 2下线 3不通过',
  `reject_reason`  VARCHAR(256) NULL,
  `category`       VARCHAR(32) NULL,
  `tags`           JSON NULL COMMENT '标签JSON数组',
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status_time` (`status`, `created_at`),
  KEY `idx_category_status` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='创作者图纸表';
