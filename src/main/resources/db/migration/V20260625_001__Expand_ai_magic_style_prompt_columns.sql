ALTER TABLE bp_ai_magic_style
  MODIFY COLUMN prompt_template TEXT NULL COMMENT 'AI正向提示词模板',
  MODIFY COLUMN negative_prompt_template TEXT NULL COMMENT 'AI反向提示词模板';
