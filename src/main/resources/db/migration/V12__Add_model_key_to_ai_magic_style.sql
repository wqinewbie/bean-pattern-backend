ALTER TABLE bp_ai_magic_style
  ADD COLUMN model_key VARCHAR(64) NULL COMMENT 'AI服务模型配置Key' AFTER prompt_template;
