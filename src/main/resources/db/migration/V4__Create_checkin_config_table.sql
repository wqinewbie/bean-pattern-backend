CREATE TABLE IF NOT EXISTS `bp_checkin_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `continuous_days_required` INT NOT NULL DEFAULT 3 COMMENT '连续签到多少天可领取奖励',
  `reward_type` VARCHAR(32) NOT NULL DEFAULT 'AI_COUNT' COMMENT '奖励类型：AI_COUNT/VIP_DAYS',
  `reward_value` INT NOT NULL DEFAULT 1 COMMENT '奖励数量',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用签到',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到配置表';

INSERT INTO `bp_checkin_config` (`continuous_days_required`, `reward_type`, `reward_value`, `is_active`)
SELECT 3, 'AI_COUNT', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `bp_checkin_config` LIMIT 1);
