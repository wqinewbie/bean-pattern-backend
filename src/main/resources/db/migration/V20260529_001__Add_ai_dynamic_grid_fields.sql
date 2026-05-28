ALTER TABLE bp_ai_generate_task
  ADD COLUMN raw_ai_image_url VARCHAR(1024) NULL COMMENT 'AI 原始生成图 URL' AFTER ai_image_url,
  ADD COLUMN grid_min INT NULL COMMENT '用户选择的最小网格尺寸' AFTER size_mode,
  ADD COLUMN grid_max INT NULL COMMENT '用户选择的最大网格尺寸' AFTER grid_min,
  ADD COLUMN detected_grid_width INT NULL COMMENT 'Perfect Pixel 检测网格宽度' AFTER raw_ai_image_url,
  ADD COLUMN detected_grid_height INT NULL COMMENT 'Perfect Pixel 检测网格高度' AFTER detected_grid_width,
  ADD COLUMN final_grid_width INT NULL COMMENT '最终采样网格宽度' AFTER detected_grid_height,
  ADD COLUMN final_grid_height INT NULL COMMENT '最终采样网格高度' AFTER final_grid_width,
  ADD COLUMN perfect_pixel_status VARCHAR(32) NULL COMMENT 'SUCCESS/FAILED/SKIPPED' AFTER final_grid_height,
  ADD COLUMN perfect_pixel_error VARCHAR(1024) NULL COMMENT 'Perfect Pixel 错误信息' AFTER perfect_pixel_status;
