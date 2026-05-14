UPDATE bp_user
SET vip_level = 1,
    updated_at = CURRENT_TIMESTAMP
WHERE vip_expire_at IS NOT NULL
  AND vip_expire_at > NOW()
  AND vip_level = 0;
