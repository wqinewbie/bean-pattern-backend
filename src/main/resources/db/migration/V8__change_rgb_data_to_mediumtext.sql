-- V8__change_rgb_data_to_mediumtext.sql
-- 修改 rgb_data 字段类型为 MEDIUMTEXT 以支持大尺寸图纸（如 104x104）

-- 修改 bp_box 表的 rgb_data 字段
ALTER TABLE `bp_box` MODIFY COLUMN `rgb_data` MEDIUMTEXT NULL COMMENT '原始RGB数组';

-- 修改 bp_history 表的 rgb_data 字段
ALTER TABLE `bp_history` MODIFY COLUMN `rgb_data` MEDIUMTEXT NULL COMMENT '原始RGB数组';

-- 修改 bp_draft 表的 rgb_data 字段
ALTER TABLE `bp_draft` MODIFY COLUMN `rgb_data` MEDIUMTEXT NULL COMMENT '原始RGB数组';
