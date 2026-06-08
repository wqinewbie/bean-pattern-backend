ALTER TABLE bp_ai_magic_style
  ADD COLUMN negative_prompt_template VARCHAR(1024) NULL COMMENT 'AI反向提示词模板' AFTER prompt_template;
