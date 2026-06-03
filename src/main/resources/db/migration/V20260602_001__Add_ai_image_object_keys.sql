ALTER TABLE bp_ai_generate_task
  ADD COLUMN ai_image_key VARCHAR(1024) NULL COMMENT 'AI generated image object key' AFTER ai_image_url,
  ADD COLUMN raw_ai_image_key VARCHAR(1024) NULL COMMENT 'AI raw generated image object key' AFTER raw_ai_image_url;
