-- =====================================================
-- 添加 history_id 字段到 bp_box 表
-- Version: 7
-- Date: 2026-04-18
-- =====================================================

-- 给 bp_box 表添加 history_id 字段（关联时光机ID）
ALTER TABLE `bp_box` 
  ADD COLUMN `history_id` BIGINT NULL COMMENT '关联时光机ID' AFTER `draft_id`;

-- 给 bp_history 表添加索引
ALTER TABLE `bp_history` 
  ADD INDEX `idx_history_box_id` (`box_id`);
