CREATE TABLE IF NOT EXISTS `bp_checkin_config` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `continuous_days_required` INT NOT NULL DEFAULT 3 COMMENT '连续签到多少天可领取奖励',
  `gift_package_code` VARCHAR(64) NULL COMMENT '签到奖励礼品包编码',
  `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用签到',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到配置表';

ALTER TABLE `bp_checkin_config` ADD COLUMN IF NOT EXISTS `gift_package_code` VARCHAR(64) NULL COMMENT '签到奖励礼品包编码' AFTER `continuous_days_required`;

INSERT INTO `bp_checkin_config` (`continuous_days_required`, `gift_package_code`, `is_active`)
SELECT 3, NULL, 1
WHERE NOT EXISTS (SELECT 1 FROM `bp_checkin_config` LIMIT 1);
