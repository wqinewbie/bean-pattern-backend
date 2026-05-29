ALTER TABLE bp_ai_generate_task
  ADD COLUMN mapped_pixel_data MEDIUMTEXT NULL COMMENT '处理后的像素数据 JSON' AFTER perfect_pixel_error,
  ADD COLUMN history_id BIGINT NULL COMMENT '关联的时光机记录ID' AFTER mapped_pixel_data;

ALTER TABLE bp_history
  ADD COLUMN task_id VARCHAR(64) NULL COMMENT '关联的AI任务ID' AFTER user_id,
  ADD INDEX idx_history_task_id (task_id);
