ALTER TABLE bp_history
  ADD COLUMN IF NOT EXISTS ai_style VARCHAR(64) NULL COMMENT 'AI生成风格' AFTER mapped_pixel_data;

ALTER TABLE bp_box
  ADD COLUMN IF NOT EXISTS ai_style VARCHAR(64) NULL COMMENT 'AI生成风格' AFTER mapped_pixel_data;
