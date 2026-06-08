ALTER TABLE bp_ai_quota_log
ADD COLUMN ai_generate_refund_key VARCHAR(255)
GENERATED ALWAYS AS (
  CASE
    WHEN change_type = 'REFUND'
      AND biz_type IN ('AI_GENERATE_FAILED', 'AI_GENERATE_TIMEOUT')
      AND biz_id IS NOT NULL
      AND biz_id <> ''
    THEN CONCAT(user_id, ':', biz_type, ':', biz_id)
    ELSE NULL
  END
) STORED,
ADD UNIQUE KEY uk_ai_generate_refund_key (ai_generate_refund_key);
