-- 移除 Banner 背景色字段
-- MySQL 8.0.29+ 可直接执行，低版本请手动执行
ALTER TABLE `bp_banner` DROP COLUMN `bg_color`;
