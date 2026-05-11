-- 为 AI 生成任务扣减日志增加幂等唯一索引，避免同一任务重复扣次
ALTER TABLE bp_ai_quota_log
ADD COLUMN ai_generate_use_key VARCHAR(255)
GENERATED ALWAYS AS (
  CASE
    WHEN change_type = 'USE' AND biz_type = 'AI_GENERATE' AND biz_id IS NOT NULL AND biz_id <> ''
    THEN CONCAT(user_id, ':', biz_id)
    ELSE NULL
  END
) STORED,
ADD UNIQUE KEY uk_ai_generate_use_key (ai_generate_use_key);
