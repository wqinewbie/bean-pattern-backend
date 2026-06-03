ALTER TABLE bp_box
  ADD COLUMN IF NOT EXISTS focus_progress TEXT NULL COMMENT '沉浸模式进度JSON' AFTER mapped_pixel_data,
  ADD COLUMN IF NOT EXISTS focus_completed_cells INT NOT NULL DEFAULT 0 COMMENT '沉浸模式已完成格子数' AFTER focus_progress,
  ADD COLUMN IF NOT EXISTS focus_total_cells INT NOT NULL DEFAULT 0 COMMENT '沉浸模式总格子数' AFTER focus_completed_cells;
