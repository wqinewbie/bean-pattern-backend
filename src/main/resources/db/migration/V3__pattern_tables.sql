-- =====================================================
-- 拼豆V1.0 图纸相关表结构
-- Version: 3.1
-- Date: 2026-04-18
-- =====================================================

-- -----------------------------------------------------
-- 图纸箱 bp_box
-- 存储用户的成品图纸，支持多种来源（图片转图纸/AI生成/画板）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bp_box` (
  `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`         BIGINT NOT NULL COMMENT '用户ID',
  
  -- 来源类型
  `source_type`     ENUM('LOCAL', 'AI', 'DRAW') NOT NULL COMMENT 'LOCAL=图片转图纸 AI=AI生成 DRAW=画板',
  
  -- 【核心】品牌和色数（用户选择）
  `brand`           VARCHAR(32) NOT NULL COMMENT '品牌，如mard',
  `color_count`     INT NOT NULL COMMENT '色数，如48',
  
  -- 图纸基本信息
  `name`            VARCHAR(128) NULL COMMENT '图纸名称',
  `grid_size`       INT NOT NULL COMMENT '尺寸，如64表示64x64',
  
  -- 【核心】图纸数据
  `rgb_data`        TEXT NULL COMMENT '原始RGB数组，如[[[255,0,0],[0,255,0]],...]',
  `grid_data`       TEXT NOT NULL COMMENT '色号索引数组，如[[0,1,2],[3,1,0],...]',
  `color_palette`   TEXT NOT NULL COMMENT '颜色表JSON，如[{id:"001",name:"红色",r:255,g:0,b:0},...]',
  
  -- 关联关系
  `draft_id`        BIGINT NULL COMMENT '关联草稿箱ID（如果从草稿保存）',
  
  -- 原图（可选，图片转图纸来源才有）
  `source_url`      VARCHAR(1024) NULL COMMENT '原始图片URL',
  
  -- 状态
  `status`          TINYINT NOT NULL DEFAULT 0 COMMENT '0=处理中 1=已完成 2=已失效',
  
  -- 时间戳
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_source_type` (`source_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图纸箱表';

-- -----------------------------------------------------
-- 时光机 bp_history
-- 存储用户的生成操作日志（图片转图纸/AI生成记录）
-- 30天后自动过期清理
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bp_history` (
  `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`         BIGINT NOT NULL COMMENT '用户ID',
  
  -- 来源类型
  `source_type`     ENUM('LOCAL', 'AI') NOT NULL COMMENT 'LOCAL=图片转图纸 AI=AI生成',
  
  -- 【核心】品牌和色数
  `brand`           VARCHAR(32) NOT NULL COMMENT '品牌，如mard',
  `color_count`     INT NOT NULL COMMENT '色数，如48',
  
  -- 图纸基本信息
  `name`            VARCHAR(128) NULL COMMENT '图纸名称',
  `grid_size`       INT NOT NULL COMMENT '尺寸，如64表示64x64',
  
  -- 【核心】图纸数据
  `rgb_data`        TEXT NULL COMMENT '原始RGB数组',
  `grid_data`       TEXT NOT NULL COMMENT '色号索引数组',
  `color_palette`   TEXT NOT NULL COMMENT '颜色表JSON',
  
  -- 关联关系
  `box_id`          BIGINT NULL COMMENT '关联图纸箱ID（如果已保存到图纸箱）',
  
  -- 原图
  `source_url`      VARCHAR(1024) NULL COMMENT '原始图片URL',
  
  -- 过期时间（30天后清理）
  `expires_at`      DATETIME NULL COMMENT '过期时间，30天后自动清理',
  
  -- 时间戳
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_expires_at` (`expires_at`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='时光机表（操作日志）';

-- -----------------------------------------------------
-- 草稿箱 bp_draft
-- 存储用户的半成品图纸（画板/编辑模式）
-- 30天后自动过期清理
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `bp_draft` (
  `id`              BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`         BIGINT NOT NULL COMMENT '用户ID',
  
  -- 来源类型
  `source_type`     ENUM('DRAW', 'EDIT') NOT NULL COMMENT 'DRAW=画板 EDIT=编辑模式',
  
  -- 【核心】品牌和色数（画板模式下可能为空）
  `brand`           VARCHAR(32) NULL COMMENT '品牌，如mard',
  `color_count`     INT NULL COMMENT '色数，如48',
  
  -- 图纸基本信息
  `name`            VARCHAR(128) NULL COMMENT '图纸名称',
  `grid_size`       INT NOT NULL COMMENT '尺寸，如64表示64x64',
  
  -- 【核心】图纸数据
  `rgb_data`        TEXT NULL COMMENT '原始RGB数组（画板模式下可能为空）',
  `grid_data`       TEXT NOT NULL COMMENT '色号索引数组',
  `color_palette`   TEXT NOT NULL COMMENT '颜色表JSON',
  
  -- 关联关系
  `box_id`          BIGINT NULL COMMENT '关联图纸箱ID（如果已保存到图纸箱）',
  
  -- 过期时间（30天后清理）
  `expires_at`      DATETIME NULL COMMENT '过期时间，30天后自动清理',
  
  -- 时间戳
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_expires_at` (`expires_at`),
  INDEX `idx_updated_at` (`updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='草稿箱表';
