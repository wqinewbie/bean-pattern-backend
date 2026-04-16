-- 教程管理表
CREATE TABLE IF NOT EXISTS `bp_tutorial` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`         VARCHAR(255) NOT NULL COMMENT '教程标题',
  `description`   VARCHAR(512) NULL COMMENT '教程描述',
  `video_url`     VARCHAR(1024) NOT NULL COMMENT '视频地址',
  `thumbnail_url` VARCHAR(1024) NULL COMMENT '缩略图地址',
  `sort_order`    INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status`        TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0下线 1上线',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教程表';
