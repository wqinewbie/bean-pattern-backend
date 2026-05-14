-- =========================
-- Bean Pattern Backend
-- Version: 3.0
-- =========================

CREATE TABLE IF NOT EXISTS `bp_user` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `open_id`       VARCHAR(64) NOT NULL COMMENT '微信openid',
  `union_id`      VARCHAR(64) NULL COMMENT '微信unionid',
  `nick_name`     VARCHAR(64) NULL COMMENT '用户昵称',
  `avatar_url`    VARCHAR(1024) NULL COMMENT '头像URL',
  `phone`         VARCHAR(20) NULL COMMENT '手机号',
  `gender`        TINYINT(1) NULL DEFAULT 0 COMMENT '0未知 1男 2女',
  `vip_level`     TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0普通 1高级',
  `vip_expire_at` DATETIME NULL COMMENT 'VIP到期时间',
  `magic_coins`   INT NOT NULL DEFAULT 0 COMMENT '金币余额',
  `ai_quota`      INT NOT NULL DEFAULT 3 COMMENT 'AI剩余次数',
  `status`        TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `bp_image_task` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT NOT NULL COMMENT '用户ID',
  `task_type`     VARCHAR(32) NOT NULL COMMENT 'IMAGE_PROCESS/BEAD_LOCAL/BEAD_AI',
  `source_url`    VARCHAR(1024) NULL,
  `result_url`    VARCHAR(1024) NULL,
  `pattern_url`   VARCHAR(1024) NULL,
  `prompt`        VARCHAR(512) NULL,
  `style`         VARCHAR(64) NULL,
  `grid_size`     INT NULL,
  `color_stats`   TEXT NULL,
  `status`        VARCHAR(32) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/SUCCESS/FAILED',
  `error_message` VARCHAR(1024) NULL,
  `is_saved`      TINYINT(1) NOT NULL DEFAULT 0,
  `is_public`     TINYINT(1) NOT NULL DEFAULT 0,
  `title`         VARCHAR(128) NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_type_time` (`user_id`, `task_type`, `created_at`),
  KEY `idx_is_public_time` (`is_public`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='任务记录表';

CREATE TABLE IF NOT EXISTS `bp_banner` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title`       VARCHAR(128) NOT NULL,
  `sub_title`   VARCHAR(256) NULL,
  `image_url`   VARCHAR(1024) NOT NULL,
  `link_type`   VARCHAR(32) NULL COMMENT 'PAGE/URL/NONE',
  `link_value`  VARCHAR(512) NULL,
  `tag_text`    VARCHAR(32) NULL,
  `sort_order`  INT NOT NULL DEFAULT 0,
  `status`      TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0下线 1上线',
  `start_at`    DATETIME NULL,
  `end_at`      DATETIME NULL,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Banner表';

CREATE TABLE IF NOT EXISTS `bp_banner_claim_log` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT NOT NULL COMMENT '用户ID',
  `banner_id`   BIGINT NOT NULL COMMENT 'Banner ID',
  `banner_code` VARCHAR(64) NOT NULL COMMENT 'Banner业务编码',
  `gift_type`   VARCHAR(64) NOT NULL COMMENT '礼品类型',
  `gift_value`  INT NOT NULL DEFAULT 0 COMMENT '礼品值',
  `claim_date`  DATE NOT NULL COMMENT '领取日期',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_banner` (`user_id`, `banner_code`),
  KEY `idx_user_banner_date` (`user_id`, `banner_code`, `claim_date`),
  KEY `idx_banner_id` (`banner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Banner领取记录表';

CREATE TABLE IF NOT EXISTS `bp_recharge_plan` (
  `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name`           VARCHAR(64) NOT NULL,
  `description`    VARCHAR(256) NULL,
  `coins`          INT NOT NULL DEFAULT 0,
  `ai_quota`       INT NOT NULL DEFAULT 0,
  `price`          DECIMAL(10,2) NOT NULL,
  `original_price` DECIMAL(10,2) NULL,
  `is_vip`         TINYINT(1) NOT NULL DEFAULT 0,
  `vip_days`       INT NOT NULL DEFAULT 0,
  `tag`            VARCHAR(32) NULL,
  `sort_order`     INT NOT NULL DEFAULT 0,
  `status`         TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0下线 1上线',
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='充值套餐表';

CREATE TABLE IF NOT EXISTS `bp_order` (
  `id`                   BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no`             VARCHAR(64) NOT NULL,
  `user_id`              BIGINT NOT NULL,
  `plan_id`              BIGINT NOT NULL DEFAULT 0 COMMENT '旧版充值套餐ID，会员/次卡订单默认为0',
  `plan_name`            VARCHAR(64) NOT NULL DEFAULT '',
  `product_id`           BIGINT NULL COMMENT '商品ID',
  `vip_level_purchased`  TINYINT(1) NULL COMMENT '购买的VIP等级',
  `vip_days`             INT NULL COMMENT 'VIP天数',
  `gift_items`           TEXT NULL COMMENT '礼品项JSON',
  `product_type`         VARCHAR(32) NULL COMMENT '商品类型：vip/card/gift',
  `package_code`         VARCHAR(64) NULL COMMENT '套餐代码',
  `expire_at`            DATETIME NULL COMMENT '订单过期时间',
  `deliver_status`       VARCHAR(32) NULL DEFAULT 'PENDING' COMMENT '发货状态：PENDING/SUCCESS/FAILED',
  `deliver_error`        VARCHAR(512) NULL COMMENT '发货错误信息',
  `transaction_id`       VARCHAR(64) NULL COMMENT '微信交易单号',
  `amount`               DECIMAL(10,2) NOT NULL,
  `coins`                INT NOT NULL DEFAULT 0,
  `ai_quota`             INT NOT NULL DEFAULT 0,
  `pay_type`             VARCHAR(32) NULL,
  `wx_transaction_id`    VARCHAR(64) NULL,
  `status`               VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/REFUNDED/CANCELLED',
  `paid_at`              DATETIME NULL,
  `created_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_paid_at` (`paid_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS `bp_coin_log` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT NOT NULL,
  `delta`       INT NOT NULL COMMENT '变化量（正收入负消耗）',
  `balance`     INT NOT NULL COMMENT '变化后余额快照',
  `biz_type`    VARCHAR(32) NOT NULL COMMENT 'RECHARGE/AI_USE/CREATOR_INCOME/WITHDRAW/ADMIN',
  `biz_id`      VARCHAR(64) NULL,
  `remark`      VARCHAR(256) NULL,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='金币流水表';

CREATE TABLE IF NOT EXISTS `bp_creator_pattern` (
  `id`             BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`        BIGINT NOT NULL,
  `title`          VARCHAR(128) NOT NULL,
  `description`    VARCHAR(512) NULL,
  `cover_url`      VARCHAR(1024) NOT NULL,
  `pattern_url`    VARCHAR(1024) NOT NULL,
  `grid_size`      VARCHAR(32) NULL,
  `difficulty`     TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1简单 2中等 3困难',
  `price_coins`    INT NOT NULL DEFAULT 0,
  `download_count` INT NOT NULL DEFAULT 0,
  `like_count`     INT NOT NULL DEFAULT 0,
  `income_coins`   INT NOT NULL DEFAULT 0,
  `status`         TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0审核中 1上线 2下线 3不通过',
  `reject_reason`  VARCHAR(256) NULL,
  `category`       VARCHAR(32) NULL,
  `tags`           JSON NULL COMMENT '标签JSON数组',
  `created_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status_time` (`status`, `created_at`),
  KEY `idx_category_status` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='创作者图纸表';

CREATE TABLE IF NOT EXISTS `bp_pattern_download` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT NOT NULL,
  `pattern_id`  BIGINT NOT NULL,
  `coins_spent` INT NOT NULL DEFAULT 0,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pattern` (`user_id`, `pattern_id`),
  KEY `idx_pattern` (`pattern_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图纸下载记录';

CREATE TABLE IF NOT EXISTS `bp_feedback` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT NULL COMMENT '可匿名',
  `content`     TEXT NOT NULL,
  `images`      VARCHAR(1024) NULL,
  `contact`     VARCHAR(64) NULL,
  `category`    VARCHAR(32) NULL COMMENT 'BUG/SUGGESTION/OTHER',
  `status`      TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0待处理 1处理中 2已回复 3已关闭',
  `reply`       VARCHAR(1024) NULL,
  `replied_at`  DATETIME NULL,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='反馈表';

CREATE TABLE IF NOT EXISTS `bp_sms_code` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `phone`       VARCHAR(20) NOT NULL,
  `code`        VARCHAR(8) NOT NULL,
  `biz_type`    VARCHAR(32) NOT NULL DEFAULT 'BIND' COMMENT 'BIND/LOGIN',
  `is_used`     TINYINT(1) NOT NULL DEFAULT 0,
  `expire_at`   DATETIME NOT NULL,
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_phone_biz_used` (`phone`, `biz_type`, `is_used`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信验证码表';

CREATE TABLE IF NOT EXISTS `bp_withdraw` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`       BIGINT NOT NULL,
  `coins`         INT NOT NULL,
  `amount`        DECIMAL(10,2) NOT NULL,
  `bank_info`     TEXT NULL COMMENT '收款信息JSON（敏感数据应用层加密）',
  `status`        VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/PAID',
  `reject_reason` VARCHAR(256) NULL,
  `paid_at`       DATETIME NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提现申请表';

CREATE TABLE IF NOT EXISTS `bp_admin` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`      VARCHAR(64) NOT NULL,
  `password`      VARCHAR(128) NOT NULL COMMENT 'BCrypt加密',
  `nick_name`     VARCHAR(64) NULL,
  `role`          VARCHAR(32) NOT NULL DEFAULT 'ADMIN' COMMENT 'SUPER_ADMIN/ADMIN',
  `status`        TINYINT(1) NOT NULL DEFAULT 1,
  `last_login_at` DATETIME NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- =========================
-- 初始数据
-- =========================

-- 管理员账号由 AdminInitializer 在应用首次启动时自动创建（BCrypt加密），此处不插入明文密码

INSERT INTO `bp_banner` (`title`, `sub_title`, `tag_text`, `image_url`, `link_type`, `sort_order`, `status`)
SELECT '初夏限定拼豆', '一键生成专属图纸', '魔法上新', '', 'NONE', 1, 1
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `bp_banner` LIMIT 1);

INSERT INTO `bp_recharge_plan` (`name`, `description`, `coins`, `ai_quota`, `price`, `original_price`, `is_vip`, `vip_days`, `tag`, `sort_order`)
SELECT * FROM (
  SELECT '体验包','3次AI生成',0,3,6.00,9.00,0,0,'新人专享',1 UNION ALL
  SELECT '标准包','10次AI生成 + 100金币',100,10,18.00,25.00,0,0,'推荐',2 UNION ALL
  SELECT '豪华包','30次AI生成 + 500金币',500,30,45.00,60.00,0,0,'超值',3 UNION ALL
  SELECT 'VIP月卡','无限AI + 1000金币 + VIP',1000,999,28.00,39.00,1,30,'热门',4
) AS tmp WHERE NOT EXISTS (SELECT 1 FROM `bp_recharge_plan` LIMIT 1);

-- =========================
-- 水印配置表
-- =========================

CREATE TABLE IF NOT EXISTS `bp_watermark_config` (
  `id`               BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_name`         VARCHAR(128) NOT NULL DEFAULT '拼豆魔法屋' COMMENT '小程序名称',
  `default_text`     VARCHAR(128) NOT NULL DEFAULT '拼豆魔法屋出品' COMMENT '默认水印文字',
  `font_size`        INT NOT NULL DEFAULT 24 COMMENT '字体大小',
  `color`            VARCHAR(64) NOT NULL DEFAULT 'rgba(100,100,100,0.25)' COMMENT '颜色',
  `angle`            INT NOT NULL DEFAULT -30 COMMENT '倾斜角度（度）',
  `spacing_x_ratio`  DECIMAL(3,2) NOT NULL DEFAULT 0.22 COMMENT '水平间距比例',
  `spacing_y_ratio`  DECIMAL(3,2) NOT NULL DEFAULT 0.18 COMMENT '垂直间距比例',
  `opacity`          DECIMAL(3,2) NOT NULL DEFAULT 0.25 COMMENT '透明度',
  `created_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='全局水印配置表';

CREATE TABLE IF NOT EXISTS `bp_user_watermark_config` (
  `id`          BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id`     BIGINT NOT NULL COMMENT '用户ID',
  `enabled`     TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0=关闭 1=开启',
  `custom_text` VARCHAR(128) NULL COMMENT '自定义水印文字（VIP专属）',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户水印配置表（VIP功能）';

-- 插入默认水印配置
INSERT INTO `bp_watermark_config` 
  (`app_name`, `default_text`, `font_size`, `color`, `angle`, `spacing_x_ratio`, `spacing_y_ratio`, `opacity`)
SELECT '拼豆魔法屋', '拼豆魔法屋出品', 24, 'rgba(100,100,100,0.25)', -30, 0.22, 0.18, 0.25
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `bp_watermark_config` LIMIT 1);
