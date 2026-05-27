package com.beanpattern.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 数据库字段兼容升级器
 * bead 色卡初始化也在此执行
 */
@Component
@Order(1)
public class SchemaUpgrader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaUpgrader.class);
    private final JdbcTemplate jdbc;
    private final boolean seedEnabled;

    public SchemaUpgrader(JdbcTemplate jdbc, @Value("${app.seed.enabled:false}") boolean seedEnabled) {
        this.jdbc = jdbc;
        this.seedEnabled = seedEnabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        String db = jdbc.queryForObject("SELECT DATABASE()", String.class);
        log.info("[SchemaUpgrader] 开始检查数据库字段兼容性，数据库: {}", db);

        // bp_user
        addColumn(db, "bp_user", "union_id",      "ALTER TABLE `bp_user` ADD COLUMN `union_id` VARCHAR(64) NULL COMMENT 'UnionID' AFTER `open_id`");
        addColumn(db, "bp_user", "invite_code",   "ALTER TABLE `bp_user` ADD COLUMN `invite_code` VARCHAR(32) NULL COMMENT '用户邀请码' AFTER `open_id`");
        addColumn(db, "bp_user", "phone",         "ALTER TABLE `bp_user` ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '手机号' AFTER `avatar_url`");
        addColumn(db, "bp_user", "gender",        "ALTER TABLE `bp_user` ADD COLUMN `gender` TINYINT(1) NULL DEFAULT 0 COMMENT '0未知 1男 2女' AFTER `phone`");
        addColumn(db, "bp_user", "vip_level",     "ALTER TABLE `bp_user` ADD COLUMN `vip_level` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0普通 1高级' AFTER `gender`");
        addColumn(db, "bp_user", "vip_expire_at", "ALTER TABLE `bp_user` ADD COLUMN `vip_expire_at` DATETIME NULL COMMENT 'VIP到期时间' AFTER `vip_level`");
        addColumn(db, "bp_user", "ai_quota",      "ALTER TABLE `bp_user` ADD COLUMN `ai_quota` INT NOT NULL DEFAULT 3 COMMENT 'AI剩余次数' AFTER `vip_expire_at`");
        addColumn(db, "bp_user", "status",        "ALTER TABLE `bp_user` ADD COLUMN `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0禁用 1正常'");
        addColumn(db, "bp_user", "updated_at",    "ALTER TABLE `bp_user` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP");

        // bp_image_task
        addColumn(db, "bp_image_task", "prompt",      "ALTER TABLE `bp_image_task` ADD COLUMN `prompt` VARCHAR(512) NULL AFTER `pattern_url`");
        addColumn(db, "bp_image_task", "style",       "ALTER TABLE `bp_image_task` ADD COLUMN `style` VARCHAR(64) NULL AFTER `prompt`");
        addColumn(db, "bp_image_task", "grid_size",   "ALTER TABLE `bp_image_task` ADD COLUMN `grid_size` INT NULL AFTER `style`");
        addColumn(db, "bp_image_task", "color_stats", "ALTER TABLE `bp_image_task` ADD COLUMN `color_stats` TEXT NULL AFTER `grid_size`");
        addColumn(db, "bp_image_task", "is_saved",    "ALTER TABLE `bp_image_task` ADD COLUMN `is_saved` TINYINT(1) NOT NULL DEFAULT 0 AFTER `error_message`");
        addColumn(db, "bp_image_task", "is_public",   "ALTER TABLE `bp_image_task` ADD COLUMN `is_public` TINYINT(1) NOT NULL DEFAULT 0 AFTER `is_saved`");
        addColumn(db, "bp_image_task", "title",       "ALTER TABLE `bp_image_task` ADD COLUMN `title` VARCHAR(128) NULL AFTER `is_public`");

        // bp_banner
        addColumn(db, "bp_checkin_config", "gift_package_code", "ALTER TABLE `bp_checkin_config` ADD COLUMN `gift_package_code` VARCHAR(64) NULL COMMENT '签到奖励礼品包编码' AFTER `continuous_days_required`");
        addColumn(db, "bp_activity_config", "gift_package_code", "ALTER TABLE `bp_activity_config` ADD COLUMN `gift_package_code` VARCHAR(64) NULL COMMENT '活动绑定礼品包编码' AFTER `activity_type`");
        relaxActivityClaimUniqueIndex();

        // bp_banner
        addColumn(db, "bp_banner", "bg_color",   "ALTER TABLE `bp_banner` ADD COLUMN `bg_color` VARCHAR(32) NULL DEFAULT '' AFTER `tag_text`");
        addColumn(db, "bp_banner", "action_type", "ALTER TABLE `bp_banner` ADD COLUMN `action_type` VARCHAR(32) NULL DEFAULT 'NONE' AFTER `link_value`");
        addColumn(db, "bp_banner", "action_config", "ALTER TABLE `bp_banner` ADD COLUMN `action_config` TEXT NULL AFTER `action_type`");
        addColumn(db, "bp_box", "cover_url", "ALTER TABLE `bp_box` ADD COLUMN `cover_url` VARCHAR(1024) NULL COMMENT '封面图URL' AFTER `source_url`");
        addColumn(db, "bp_banner", "start_at",   "ALTER TABLE `bp_banner` ADD COLUMN `start_at` DATETIME NULL AFTER `status`");
        addColumn(db, "bp_banner", "end_at",     "ALTER TABLE `bp_banner` ADD COLUMN `end_at` DATETIME NULL AFTER `start_at`");
        addColumn(db, "bp_banner", "updated_at", "ALTER TABLE `bp_banner` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`");
        createBannerClaimLogTable(db);
        createGiftPackageTables(db);
        createReviewTaskSubmissionTable(db);
        createUserInviteRelationTable(db);
        createWatermarkTables(db);
        createAiMagicStyleTable(db);
        ensureCommercePackageTables(db);
        ensurePopupConfigTable(db);
        createSysDictTable(db);
        seedSysDictItems();
        enforceBannerUtf8mb4();

        // bp_recharge_plan
        addColumn(db, "bp_recharge_plan", "is_vip",   "ALTER TABLE `bp_recharge_plan` ADD COLUMN `is_vip` TINYINT(1) NOT NULL DEFAULT 0 AFTER `original_price`");
        addColumn(db, "bp_recharge_plan", "vip_days", "ALTER TABLE `bp_recharge_plan` ADD COLUMN `vip_days` INT NOT NULL DEFAULT 0 AFTER `is_vip`");

        // bp_order
        addColumn(db, "bp_order", "plan_name", "ALTER TABLE `bp_order` ADD COLUMN `plan_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '套餐名称快照' AFTER `plan_id`");
        addColumn(db, "bp_order", "product_id", "ALTER TABLE `bp_order` ADD COLUMN `product_id` BIGINT NULL COMMENT '商品ID' AFTER `plan_name`");
        addColumn(db, "bp_order", "vip_level_purchased", "ALTER TABLE `bp_order` ADD COLUMN `vip_level_purchased` TINYINT(1) NULL COMMENT '购买的VIP等级' AFTER `product_id`");
        addColumn(db, "bp_order", "vip_days", "ALTER TABLE `bp_order` ADD COLUMN `vip_days` INT NULL COMMENT 'VIP天数' AFTER `vip_level_purchased`");
        addColumn(db, "bp_order", "gift_items", "ALTER TABLE `bp_order` ADD COLUMN `gift_items` TEXT NULL COMMENT '礼品项JSON' AFTER `vip_days`");
        addColumn(db, "bp_order", "product_type", "ALTER TABLE `bp_order` ADD COLUMN `product_type` VARCHAR(32) NULL COMMENT '商品类型：vip/card/gift' AFTER `gift_items`");
        addColumn(db, "bp_order", "package_code", "ALTER TABLE `bp_order` ADD COLUMN `package_code` VARCHAR(64) NULL COMMENT '套餐代码' AFTER `product_type`");
        addColumn(db, "bp_order", "expire_at", "ALTER TABLE `bp_order` ADD COLUMN `expire_at` DATETIME NULL COMMENT '订单过期时间' AFTER `package_code`");
        addColumn(db, "bp_order", "deliver_status", "ALTER TABLE `bp_order` ADD COLUMN `deliver_status` VARCHAR(32) NULL DEFAULT 'PENDING' COMMENT '发货状态：PENDING/SUCCESS/FAILED' AFTER `expire_at`");
        addColumn(db, "bp_order", "deliver_error", "ALTER TABLE `bp_order` ADD COLUMN `deliver_error` VARCHAR(512) NULL COMMENT '发货错误信息' AFTER `deliver_status`");
        addColumn(db, "bp_order", "transaction_id", "ALTER TABLE `bp_order` ADD COLUMN `transaction_id` VARCHAR(64) NULL COMMENT '微信交易单号' AFTER `deliver_error`");
        addColumn(db, "bp_order", "coupon_id", "ALTER TABLE `bp_order` ADD COLUMN `coupon_id` BIGINT NULL COMMENT '使用的优惠券ID' AFTER `transaction_id`");

        // user_gift - 修改 gift_item_id 为可空，支持礼品包动态生成的礼品
        relaxUserGiftItemIdConstraint(db);

        // bp_feedback
        addColumn(db, "bp_feedback", "updated_at", "ALTER TABLE `bp_feedback` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`");

        // bp_admin
        addColumn(db, "bp_admin", "updated_at", "ALTER TABLE `bp_admin` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`");

        // bp_creator_pattern
        addColumn(db, "bp_creator_pattern", "income_coins",  "ALTER TABLE `bp_creator_pattern` ADD COLUMN `income_coins` INT NOT NULL DEFAULT 0 AFTER `like_count`");
        addColumn(db, "bp_creator_pattern", "reject_reason", "ALTER TABLE `bp_creator_pattern` ADD COLUMN `reject_reason` VARCHAR(256) NULL AFTER `status`");
        addColumn(db, "bp_creator_pattern", "category",      "ALTER TABLE `bp_creator_pattern` ADD COLUMN `category` VARCHAR(32) NULL AFTER `reject_reason`");
        addColumn(db, "bp_creator_pattern", "tags",          "ALTER TABLE `bp_creator_pattern` ADD COLUMN `tags` JSON NULL AFTER `category`");

        // bead 色卡相关表 + 数据
        createBeadTables();

        // 初始数据（幂等）
        if (seedEnabled) {
            seedData();
        } else {
            log.info("[SchemaUpgrader] seed is disabled, skip seedData");
        }

        // 用户资料完整性约束：昵称与头像不能为空
        enforceUserProfileRequired();

        log.info("[SchemaUpgrader] 字段兼容性检查完成");
    }

    private void relaxActivityClaimUniqueIndex() {
        try {
            jdbc.execute("ALTER TABLE bp_user_activity_log DROP INDEX uk_user_activity_action");
            log.info("[SchemaUpgrader] 已移除活动领取 ONCE 唯一索引，改由业务按 limit_type 控制");
        } catch (Exception ignored) {
        }
    }

    private void relaxUserGiftItemIdConstraint(String db) {
        try {
            // 检查表是否存在
            Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'user_gift'",
                Integer.class, db);
            if (count == null || count == 0) {
                log.info("[SchemaUpgrader] user_gift 表不存在，跳过字段修改");
                return;
            }

            // 修改 gift_item_id 为可空
            jdbc.execute("ALTER TABLE user_gift MODIFY COLUMN gift_item_id BIGINT NULL COMMENT '礼品项ID（礼品包动态生成的礼品可为空）'");
            log.info("[SchemaUpgrader] 已将 user_gift.gift_item_id 修改为可空");
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 修改 user_gift.gift_item_id 约束失败: {}", e.getMessage());
        }
    }

    private void createBannerClaimLogTable(String db) {
        createTableIfNotExists(db, "bp_banner_claim_log",
                "CREATE TABLE bp_banner_claim_log (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键'," +
                        "user_id BIGINT NOT NULL COMMENT '用户ID'," +
                        "banner_id BIGINT NOT NULL COMMENT 'Banner ID'," +
                        "banner_code VARCHAR(64) NOT NULL COMMENT 'Banner业务编码'," +
                        "gift_type VARCHAR(64) NOT NULL COMMENT '礼品类型'," +
                        "gift_value INT NOT NULL DEFAULT 0 COMMENT '礼品值'," +
                        "claim_date DATE NOT NULL COMMENT '领取日期'," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "KEY idx_user_banner(user_id, banner_code)," +
                        "KEY idx_user_banner_date(user_id, banner_code, claim_date)," +
                        "KEY idx_banner_id(banner_id)" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='Banner领取记录表'");
    }

    private void createGiftPackageTables(String db) {
        createTableIfNotExists(db, "bp_gift_type",
                "CREATE TABLE bp_gift_type (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "code VARCHAR(64) NOT NULL UNIQUE COMMENT '礼品类型编码'," +
                        "name VARCHAR(128) NOT NULL COMMENT '礼品类型名称'," +
                        "gift_category VARCHAR(64) NULL COMMENT '礼品分类'," +
                        "description VARCHAR(255) NULL COMMENT '描述'," +
                        "icon_url VARCHAR(512) NULL COMMENT '图标'," +
                        "sort_order INT NOT NULL DEFAULT 0 COMMENT '排序'," +
                        "status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用'," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='礼品类型表'");
        addColumn(db, "bp_gift_type", "value_type", "ALTER TABLE `gift_type` ADD COLUMN `value_type` VARCHAR(32) NOT NULL DEFAULT 'number' COMMENT '值类型：number/discount/days/times/coins' AFTER `status`");
        addColumn(db, "gift_type", "target_product_type", "ALTER TABLE `gift_type` ADD COLUMN `target_product_type` VARCHAR(32) NULL COMMENT '适用商品：vip/card/all' AFTER `value_type`");
        seedGiftTypes();

        createTableIfNotExists(db, "bp_gift_package",
                "CREATE TABLE bp_gift_package (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "package_code VARCHAR(64) NOT NULL UNIQUE COMMENT '礼品包编码'," +
                        "name VARCHAR(128) NOT NULL COMMENT '礼品包名称'," +
                        "description VARCHAR(255) NULL COMMENT '描述'," +
                        "items_json TEXT NOT NULL COMMENT '礼品明细JSON'," +
                        "status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1启用 0停用'," +
                        "sort_order INT NOT NULL DEFAULT 0 COMMENT '排序'," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='礼品包表'");
        addColumn(db, "bp_gift_package", "expire_days", "ALTER TABLE `bp_gift_package` ADD COLUMN `expire_days` INT NOT NULL DEFAULT 30 COMMENT '有效期天数' AFTER `status`");
    }

    private void createReviewTaskSubmissionTable(String db) {
        createTableIfNotExists(db, "bp_review_task_submission",
                "CREATE TABLE bp_review_task_submission (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "user_id BIGINT NOT NULL COMMENT '用户ID'," +
                        "task_id BIGINT NOT NULL COMMENT '任务ID'," +
                        "task_code VARCHAR(64) NOT NULL COMMENT '任务编码'," +
                        "submission_text VARCHAR(500) NULL COMMENT '提交说明'," +
                        "proof_images TEXT NULL COMMENT '凭证图片JSON数组'," +
                        "status TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1审核通过 2审核驳回'," +
                        "review_remark VARCHAR(500) NULL COMMENT '审核备注'," +
                        "reviewed_at DATETIME NULL COMMENT '审核时间'," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "KEY idx_user_task(user_id, task_code)," +
                        "KEY idx_status(status)," +
                        "KEY idx_task_code(task_code)" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='审核型任务提交表'");
    }

    private void createUserInviteRelationTable(String db) {
        createTableIfNotExists(db, "bp_user_invite_relation",
                "CREATE TABLE bp_user_invite_relation (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "inviter_user_id BIGINT NOT NULL COMMENT '邀请人用户ID'," +
                        "invitee_user_id BIGINT NOT NULL COMMENT '被邀请人用户ID'," +
                        "invite_code VARCHAR(32) NOT NULL COMMENT '邀请码'," +
                        "status TINYINT NOT NULL DEFAULT 1 COMMENT '1已注册 2已首充'," +
                        "first_paid_at DATETIME NULL COMMENT '首次充值时间'," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uk_invitee(invitee_user_id)," +
                        "KEY idx_inviter(inviter_user_id)," +
                        "KEY idx_invite_code(invite_code)" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='用户邀请关系表'");
    }

    private void createWatermarkTables(String db) {
        createTableIfNotExists(db, "bp_watermark_config",
                "CREATE TABLE bp_watermark_config (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键'," +
                        "app_name VARCHAR(128) NOT NULL DEFAULT '拼豆魔法屋' COMMENT '小程序名称'," +
                        "default_text VARCHAR(128) NOT NULL DEFAULT '拼豆魔法屋出品' COMMENT '默认水印文字'," +
                        "font_size INT NOT NULL DEFAULT 24 COMMENT '字体大小'," +
                        "color VARCHAR(64) NOT NULL DEFAULT 'rgba(100,100,100,0.25)' COMMENT '颜色'," +
                        "angle INT NOT NULL DEFAULT -30 COMMENT '倾斜角度（度）'," +
                        "spacing_x_ratio DECIMAL(3,2) NOT NULL DEFAULT 0.22 COMMENT '水平间距比例'," +
                        "spacing_y_ratio DECIMAL(3,2) NOT NULL DEFAULT 0.18 COMMENT '垂直间距比例'," +
                        "opacity DECIMAL(3,2) NOT NULL DEFAULT 0.25 COMMENT '透明度'," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='全局水印配置表'");

        createTableIfNotExists(db, "bp_user_watermark_config",
                "CREATE TABLE bp_user_watermark_config (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键'," +
                        "user_id BIGINT NOT NULL COMMENT '用户ID'," +
                        "enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0=关闭 1=开启'," +
                        "custom_text VARCHAR(128) NULL COMMENT '自定义水印文字（VIP专属）'," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uk_user_id(user_id)" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='用户水印配置表（VIP功能）'");

        addColumn(db, "bp_watermark_config", "app_name", "ALTER TABLE `bp_watermark_config` ADD COLUMN `app_name` VARCHAR(128) NOT NULL DEFAULT 'PinBean' AFTER `id`");
        addColumn(db, "bp_watermark_config", "default_text", "ALTER TABLE `bp_watermark_config` ADD COLUMN `default_text` VARCHAR(128) NOT NULL DEFAULT 'PinBean' AFTER `app_name`");
        addColumn(db, "bp_watermark_config", "font_size", "ALTER TABLE `bp_watermark_config` ADD COLUMN `font_size` INT NOT NULL DEFAULT 24 AFTER `default_text`");
        addColumn(db, "bp_watermark_config", "color", "ALTER TABLE `bp_watermark_config` ADD COLUMN `color` VARCHAR(64) NOT NULL DEFAULT 'rgba(100,100,100,0.25)' AFTER `font_size`");
        addColumn(db, "bp_watermark_config", "angle", "ALTER TABLE `bp_watermark_config` ADD COLUMN `angle` INT NOT NULL DEFAULT -30 AFTER `color`");
        addColumn(db, "bp_watermark_config", "spacing_x_ratio", "ALTER TABLE `bp_watermark_config` ADD COLUMN `spacing_x_ratio` DECIMAL(5,2) NOT NULL DEFAULT 0.22 AFTER `angle`");
        addColumn(db, "bp_watermark_config", "spacing_y_ratio", "ALTER TABLE `bp_watermark_config` ADD COLUMN `spacing_y_ratio` DECIMAL(5,2) NOT NULL DEFAULT 0.18 AFTER `spacing_x_ratio`");
        addColumn(db, "bp_watermark_config", "opacity", "ALTER TABLE `bp_watermark_config` ADD COLUMN `opacity` DECIMAL(5,2) NOT NULL DEFAULT 0.25 AFTER `spacing_y_ratio`");
        addColumn(db, "bp_watermark_config", "created_at", "ALTER TABLE `bp_watermark_config` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `opacity`");
        addColumn(db, "bp_watermark_config", "updated_at", "ALTER TABLE `bp_watermark_config` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`");

        addColumn(db, "bp_user_watermark_config", "user_id", "ALTER TABLE `bp_user_watermark_config` ADD COLUMN `user_id` BIGINT NOT NULL AFTER `id`");
        addColumn(db, "bp_user_watermark_config", "enabled", "ALTER TABLE `bp_user_watermark_config` ADD COLUMN `enabled` TINYINT(1) NOT NULL DEFAULT 1 AFTER `user_id`");
        addColumn(db, "bp_user_watermark_config", "custom_text", "ALTER TABLE `bp_user_watermark_config` ADD COLUMN `custom_text` VARCHAR(128) NULL AFTER `enabled`");
        addColumn(db, "bp_user_watermark_config", "created_at", "ALTER TABLE `bp_user_watermark_config` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `custom_text`");
        addColumn(db, "bp_user_watermark_config", "updated_at", "ALTER TABLE `bp_user_watermark_config` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`");

        seedWatermarkConfig();
    }

    private void createAiMagicStyleTable(String db) {
        createTableIfNotExists(db, "bp_ai_magic_style",
                "CREATE TABLE bp_ai_magic_style (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "name VARCHAR(64) NOT NULL," +
                        "icon VARCHAR(512) NULL," +
                        "tag VARCHAR(64) NULL," +
                        "description VARCHAR(256) NULL," +
                        "prompt_template VARCHAR(512) NULL," +
                        "model_key VARCHAR(64) NULL," +
                        "sort_order INT NOT NULL DEFAULT 0," +
                        "enabled TINYINT(1) NOT NULL DEFAULT 1," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "KEY idx_enabled_sort(enabled, sort_order)" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='AI magic style config'");
        addColumn(db, "bp_ai_magic_style", "icon", "ALTER TABLE `bp_ai_magic_style` ADD COLUMN `icon` VARCHAR(512) NULL AFTER `name`");
        addColumn(db, "bp_ai_magic_style", "tag", "ALTER TABLE `bp_ai_magic_style` ADD COLUMN `tag` VARCHAR(64) NULL AFTER `icon`");
        addColumn(db, "bp_ai_magic_style", "description", "ALTER TABLE `bp_ai_magic_style` ADD COLUMN `description` VARCHAR(256) NULL AFTER `tag`");
        addColumn(db, "bp_ai_magic_style", "prompt_template", "ALTER TABLE `bp_ai_magic_style` ADD COLUMN `prompt_template` VARCHAR(512) NULL AFTER `description`");
        addColumn(db, "bp_ai_magic_style", "model_key", "ALTER TABLE `bp_ai_magic_style` ADD COLUMN `model_key` VARCHAR(64) NULL AFTER `prompt_template`");
        addColumn(db, "bp_ai_magic_style", "sort_order", "ALTER TABLE `bp_ai_magic_style` ADD COLUMN `sort_order` INT NOT NULL DEFAULT 0 AFTER `model_key`");
        addColumn(db, "bp_ai_magic_style", "enabled", "ALTER TABLE `bp_ai_magic_style` ADD COLUMN `enabled` TINYINT(1) NOT NULL DEFAULT 1 AFTER `sort_order`");
        addColumn(db, "bp_ai_magic_style", "created_at", "ALTER TABLE `bp_ai_magic_style` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `enabled`");
        addColumn(db, "bp_ai_magic_style", "updated_at", "ALTER TABLE `bp_ai_magic_style` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`");
        copyLegacyAiMagicStyles(db);
    }

    private void copyLegacyAiMagicStyles(String db) {
        try {
            Integer legacyCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME='ai_magic_style'",
                    Integer.class, db);
            if (legacyCount == null || legacyCount == 0) return;
            jdbc.execute("INSERT IGNORE INTO bp_ai_magic_style(id, name, icon, tag, description, prompt_template, sort_order, enabled, created_at, updated_at) " +
                    "SELECT id, name, icon, tag, description, prompt_template, COALESCE(sort_order, 0), COALESCE(enabled, 1), COALESCE(created_at, NOW()), COALESCE(updated_at, NOW()) " +
                    "FROM ai_magic_style");
            log.info("[SchemaUpgrader] legacy ai_magic_style copied to bp_ai_magic_style");
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] copy legacy ai_magic_style failed: {}", e.getMessage());
        }
    }

    private void ensureCommercePackageTables(String db) {
        createTableIfNotExists(db, "bp_vip_package",
                "CREATE TABLE bp_vip_package (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "package_code VARCHAR(64) NOT NULL UNIQUE," +
                        "midas_product_id VARCHAR(128) NULL," +
                        "package_name VARCHAR(128) NOT NULL," +
                        "duration_days INT NOT NULL DEFAULT 30," +
                        "price DECIMAL(10,2) NOT NULL DEFAULT 0," +
                        "original_price DECIMAL(10,2) NOT NULL DEFAULT 0," +
                        "ai_quota_gift INT NOT NULL DEFAULT 0," +
                        "tag VARCHAR(64) NULL," +
                        "sort_order INT NOT NULL DEFAULT 0," +
                        "is_active TINYINT(1) NOT NULL DEFAULT 1," +
                        "purchase_limit INT NULL," +
                        "shelf_start_time DATETIME NULL," +
                        "shelf_end_time DATETIME NULL," +
                        "vip_only TINYINT(1) NOT NULL DEFAULT 0," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='VIP package config'");
        addColumn(db, "bp_vip_package", "midas_product_id", "ALTER TABLE `bp_vip_package` ADD COLUMN `midas_product_id` VARCHAR(128) NULL AFTER `package_code`");
        addColumn(db, "bp_vip_package", "purchase_limit", "ALTER TABLE `bp_vip_package` ADD COLUMN `purchase_limit` INT NULL AFTER `is_active`");
        addColumn(db, "bp_vip_package", "shelf_start_time", "ALTER TABLE `bp_vip_package` ADD COLUMN `shelf_start_time` DATETIME NULL AFTER `purchase_limit`");
        addColumn(db, "bp_vip_package", "shelf_end_time", "ALTER TABLE `bp_vip_package` ADD COLUMN `shelf_end_time` DATETIME NULL AFTER `shelf_start_time`");
        addColumn(db, "bp_vip_package", "vip_only", "ALTER TABLE `bp_vip_package` ADD COLUMN `vip_only` TINYINT(1) NOT NULL DEFAULT 0 AFTER `shelf_end_time`");

        createTableIfNotExists(db, "bp_card_package",
                "CREATE TABLE bp_card_package (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "package_code VARCHAR(64) NOT NULL UNIQUE," +
                        "midas_product_id VARCHAR(128) NULL," +
                        "package_name VARCHAR(128) NOT NULL," +
                        "ai_quota INT NOT NULL DEFAULT 1," +
                        "price DECIMAL(10,2) NOT NULL DEFAULT 0," +
                        "original_price DECIMAL(10,2) NOT NULL DEFAULT 0," +
                        "vip_price DECIMAL(10,2) NULL," +
                        "tag VARCHAR(64) NULL," +
                        "sort_order INT NOT NULL DEFAULT 0," +
                        "is_active TINYINT(1) NOT NULL DEFAULT 1," +
                        "purchase_limit INT NULL," +
                        "shelf_start_time DATETIME NULL," +
                        "shelf_end_time DATETIME NULL," +
                        "vip_only TINYINT(1) NOT NULL DEFAULT 0," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='Card package config'");
        addColumn(db, "bp_card_package", "midas_product_id", "ALTER TABLE `bp_card_package` ADD COLUMN `midas_product_id` VARCHAR(128) NULL AFTER `package_code`");
        addColumn(db, "bp_card_package", "purchase_limit", "ALTER TABLE `bp_card_package` ADD COLUMN `purchase_limit` INT NULL AFTER `is_active`");
        addColumn(db, "bp_card_package", "shelf_start_time", "ALTER TABLE `bp_card_package` ADD COLUMN `shelf_start_time` DATETIME NULL AFTER `purchase_limit`");
        addColumn(db, "bp_card_package", "shelf_end_time", "ALTER TABLE `bp_card_package` ADD COLUMN `shelf_end_time` DATETIME NULL AFTER `shelf_start_time`");
        addColumn(db, "bp_card_package", "vip_only", "ALTER TABLE `bp_card_package` ADD COLUMN `vip_only` TINYINT(1) NOT NULL DEFAULT 0 AFTER `shelf_end_time`");
    }

    private void ensurePopupConfigTable(String db) {
        createTableIfNotExists(db, "bp_popup_config",
                "CREATE TABLE bp_popup_config (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                        "`key` VARCHAR(64) NOT NULL UNIQUE," +
                        "title VARCHAR(128) NOT NULL," +
                        "content TEXT NULL," +
                        "image_url VARCHAR(1024) NULL," +
                        "button_text VARCHAR(64) NULL," +
                        "button_url VARCHAR(512) NULL," +
                        "priority INT NOT NULL DEFAULT 0," +
                        "enabled TINYINT(1) NOT NULL DEFAULT 1," +
                        "start_time DATETIME NULL," +
                        "end_time DATETIME NULL," +
                        "show_interval INT NOT NULL DEFAULT 0," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "KEY idx_active_time(enabled, start_time, end_time)" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='Popup config'");
        addColumn(db, "bp_popup_config", "image_url", "ALTER TABLE `bp_popup_config` ADD COLUMN `image_url` VARCHAR(1024) NULL AFTER `content`");
        addColumn(db, "bp_popup_config", "button_text", "ALTER TABLE `bp_popup_config` ADD COLUMN `button_text` VARCHAR(64) NULL AFTER `image_url`");
        addColumn(db, "bp_popup_config", "button_url", "ALTER TABLE `bp_popup_config` ADD COLUMN `button_url` VARCHAR(512) NULL AFTER `button_text`");
        addColumn(db, "bp_popup_config", "priority", "ALTER TABLE `bp_popup_config` ADD COLUMN `priority` INT NOT NULL DEFAULT 0 AFTER `button_url`");
        addColumn(db, "bp_popup_config", "enabled", "ALTER TABLE `bp_popup_config` ADD COLUMN `enabled` TINYINT(1) NOT NULL DEFAULT 1 AFTER `priority`");
        addColumn(db, "bp_popup_config", "start_time", "ALTER TABLE `bp_popup_config` ADD COLUMN `start_time` DATETIME NULL AFTER `enabled`");
        addColumn(db, "bp_popup_config", "end_time", "ALTER TABLE `bp_popup_config` ADD COLUMN `end_time` DATETIME NULL AFTER `start_time`");
        addColumn(db, "bp_popup_config", "show_interval", "ALTER TABLE `bp_popup_config` ADD COLUMN `show_interval` INT NOT NULL DEFAULT 0 AFTER `end_time`");
        addColumn(db, "bp_popup_config", "created_at", "ALTER TABLE `bp_popup_config` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER `show_interval`");
        addColumn(db, "bp_popup_config", "updated_at", "ALTER TABLE `bp_popup_config` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`");
    }

    private void seedWatermarkConfig() {
        try {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM bp_watermark_config", Integer.class);
            if (count != null && count > 0) return;

            jdbc.execute("INSERT INTO bp_watermark_config(app_name, default_text, font_size, color, angle, spacing_x_ratio, spacing_y_ratio, opacity) " +
                    "VALUES ('拼豆魔法屋', '拼豆魔法屋出品', 24, 'rgba(100,100,100,0.25)', -30, 0.22, 0.18, 0.25)");
            log.info("[SchemaUpgrader] 已插入默认水印配置");
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 初始化水印配置失败: {}", e.getMessage());
        }
    }

    private void createSysDictTable(String db) {
        createTableIfNotExists(db, "bp_sys_dict_item",
                "CREATE TABLE bp_sys_dict_item (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键'," +
                        "dict_type VARCHAR(64) NOT NULL COMMENT '字典类型编码'," +
                        "dict_label VARCHAR(128) NOT NULL COMMENT '展示标签'," +
                        "dict_value VARCHAR(128) NOT NULL COMMENT '选项值'," +
                        "tag_type VARCHAR(32) NOT NULL DEFAULT 'info' COMMENT 'Tag类型'," +
                        "sort_order INT NOT NULL DEFAULT 0 COMMENT '排序'," +
                        "status TINYINT(1) NOT NULL DEFAULT 1 COMMENT '1启用0停用'," +
                        "disabled TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否禁用可选'," +
                        "remark VARCHAR(256) NULL COMMENT '备注'," +
                        "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                        "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                        "UNIQUE KEY uk_dict_type_value(dict_type, dict_value)," +
                        "KEY idx_dict_type_status(dict_type, status)" +
                        ") DEFAULT CHARSET=utf8mb4 COMMENT='系统字典项'");
    }

    private void seedSysDictItems() {
        try {
            ClassPathResource resource = new ClassPathResource("db/seed/bp_sys_dict_item_seed.sql");
            if (!resource.exists()) {
                log.warn("[SchemaUpgrader] 字典种子文件不存在: db/seed/bp_sys_dict_item_seed.sql");
                return;
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                        continue;
                    }
                    sb.append(line).append('\n');
                }
            }
            String sql = sb.toString().trim();
            if (sql.isEmpty()) {
                return;
            }
            int executed = 0;
            for (String part : sql.split(";")) {
                String stmt = part.trim();
                if (stmt.isEmpty()) {
                    continue;
                }
                try {
                    jdbc.execute(stmt);
                    executed++;
                } catch (Exception ex) {
                    log.warn("[SchemaUpgrader] 字典种子语句执行失败: {}", ex.getMessage());
                }
            }
            log.info("[SchemaUpgrader] 系统字典默认项已执行，语句数: {}", executed);
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 初始化系统字典失败: {}", e.getMessage());
        }
    }

    private void seedGiftTypes() {
        try {
            jdbc.execute("INSERT IGNORE INTO bp_gift_type(code, name, gift_category, description, sort_order, status, value_type, target_product_type) VALUES " +
                    "('AI_QUOTA', 'AI次数', 'QUOTA', '发放AI生成次数', 1, 1, 'times', 'all')," +
                    "('VIP_DAYS', '会员天数', 'MEMBERSHIP', '发放会员天数', 2, 1, 'days', 'all')," +
                    "('VIP_COUPON', '购会员卡优惠券', 'COUPON', '购买会员卡时可使用的折扣券', 4, 1, 'discount', 'vip')," +
                    "('CARD_COUPON', '购次卡优惠券', 'COUPON', '购买次卡时可使用的折扣券', 5, 1, 'discount', 'card')," +
                    "('VIP_TRIAL_CARD', '会员体验卡', 'MEMBERSHIP', '发放会员体验时长', 6, 1, 'days', 'vip')," +
                    "('VIP_CARD_COUPON', '会员专享购次卡优惠券', 'COUPON', '会员专属次卡折扣券', 7, 1, 'discount', 'card')");
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 初始化 gift_type 失败: {}", e.getMessage());
        }
    }

    private void createBeadTables() {
        try {
            String db = jdbc.queryForObject("SELECT DATABASE()", String.class);

            createTableIfNotExists(db, "bead_color",
                    "CREATE TABLE bead_color (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT," +
                            "code VARCHAR(10) NOT NULL UNIQUE COMMENT '色码编号'," +
                            "hex CHAR(7) NOT NULL," +
                            "r TINYINT UNSIGNED NOT NULL," +
                            "g TINYINT UNSIGNED NOT NULL," +
                            "b TINYINT UNSIGNED NOT NULL," +
                            "INDEX idx_code(code)" +
                            ") DEFAULT CHARSET=utf8mb4 COMMENT='拼豆色码表'");

            createTableIfNotExists(db, "bead_palette",
                    "CREATE TABLE bead_palette (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(10) NOT NULL UNIQUE COMMENT '色盘编号'," +
                            "remark VARCHAR(50) DEFAULT ''" +
                            ") DEFAULT CHARSET=utf8mb4 COMMENT='色盘表'");

            createTableIfNotExists(db, "bead_palette_color",
                    "CREATE TABLE bead_palette_color (" +
                            "palette_id INT NOT NULL," +
                            "color_id INT NOT NULL," +
                            "PRIMARY KEY(palette_id, color_id)" +
                            ") DEFAULT CHARSET=utf8mb4 COMMENT='色盘色码关联表'");

            createTableIfNotExists(db, "bead_brand",
                    "CREATE TABLE bead_brand (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT," +
                            "name VARCHAR(20) NOT NULL UNIQUE COMMENT '品牌名称'" +
                            ") DEFAULT CHARSET=utf8mb4 COMMENT='拼豆品牌表'");

            createTableIfNotExists(db, "bead_brand_kit",
                    "CREATE TABLE bead_brand_kit (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT," +
                            "brand_id INT NOT NULL," +
                            "color_count INT NOT NULL COMMENT '套装色数'," +
                            "palette_ids VARCHAR(200) NOT NULL COMMENT '色盘name逗号分隔'," +
                            "UNIQUE KEY uk_brand_count(brand_id, color_count)" +
                            ") DEFAULT CHARSET=utf8mb4 COMMENT='品牌套装表'");

            createTableIfNotExists(db, "bead_brand_kit_palette",
                    "CREATE TABLE bead_brand_kit_palette (" +
                            "kit_id INT NOT NULL COMMENT '套餐ID'," +
                            "palette_id INT NOT NULL COMMENT '色盘ID'," +
                            "sort_order INT NOT NULL DEFAULT 0 COMMENT '排序'," +
                            "PRIMARY KEY(kit_id, palette_id)," +
                            "KEY idx_palette_id(palette_id)" +
                            ") DEFAULT CHARSET=utf8mb4 COMMENT='品牌套餐色盘关联表'");

            createTableIfNotExists(db, "bead_brand_color_override",
                    "CREATE TABLE bead_brand_color_override (" +
                            "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                            "brand_id INT NOT NULL COMMENT '品牌ID'," +
                            "color_id INT NOT NULL COMMENT '颜色ID'," +
                            "display_name VARCHAR(64) NULL COMMENT '品牌颜色显示名'," +
                            "hex CHAR(7) NOT NULL," +
                            "r TINYINT UNSIGNED NOT NULL," +
                            "g TINYINT UNSIGNED NOT NULL," +
                            "b TINYINT UNSIGNED NOT NULL," +
                            "UNIQUE KEY uk_brand_color(brand_id, color_id)," +
                            "KEY idx_brand_rgb(brand_id, r, g, b)" +
                            ") DEFAULT CHARSET=utf8mb4 COMMENT='品牌颜色覆盖表'");

            seedBeadDataFromSqlFile();
            migrateBeadKitPalettes();
            migrateBrandColorOverrides(db);
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 创建bead表失败: {}", e.getMessage());
        }
    }

    private void createTableIfNotExists(String db, String table, String createSql) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME=?",
                Integer.class, db, table);
        if (count == null || count == 0) {
            jdbc.execute(createSql);
            log.info("[SchemaUpgrader] 创建表: {}", table);
        }
    }

    private void migrateBeadKitPalettes() {
        try {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM bead_brand_kit_palette", Integer.class);
            if (count != null && count > 0) return;

            List<Map<String, Object>> kits = jdbc.queryForList("SELECT id, palette_ids FROM bead_brand_kit");
            for (Map<String, Object> kit : kits) {
                Number kitIdNum = (Number) kit.get("id");
                if (kitIdNum == null) continue;
                int kitId = kitIdNum.intValue();
                String paletteIds = String.valueOf(kit.get("palette_ids") == null ? "" : kit.get("palette_ids")).trim();
                if (paletteIds.isEmpty()) continue;
                String[] names = paletteIds.split(",");
                for (int i = 0; i < names.length; i++) {
                    String name = names[i] == null ? "" : names[i].trim();
                    if (name.isEmpty()) continue;
                    List<Integer> paletteIdList = jdbc.query("SELECT id FROM bead_palette WHERE name = ?", (rs, rowNum) -> rs.getInt(1), name);
                    if (paletteIdList.isEmpty()) continue;
                    jdbc.update("INSERT IGNORE INTO bead_brand_kit_palette(kit_id, palette_id, sort_order) VALUES(?, ?, ?)", kitId, paletteIdList.get(0), i);
                }
            }
            log.info("[SchemaUpgrader] bead_brand_kit_palette 数据迁移完成");
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 迁移 bead_brand_kit_palette 失败: {}", e.getMessage());
        }
    }

    private void migrateBrandColorOverrides(String db) {
        try {
            Integer legacyExists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME='bead_brand_rgb_code'",
                    Integer.class, db
            );
            if (legacyExists == null || legacyExists == 0) return;

            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM bead_brand_color_override", Integer.class);
            if (count != null && count > 0) return;

            jdbc.execute("""
                INSERT IGNORE INTO bead_brand_color_override(brand_id, color_id, display_name, hex, r, g, b)
                SELECT b.id, c.id, c.display_name, bc.hex, bc.r, bc.g, bc.b
                FROM bead_brand_rgb_code bc
                JOIN bead_brand b ON b.name = bc.brand_name
                JOIN bead_color c ON c.code = bc.code
            """);
            log.info("[SchemaUpgrader] bead_brand_color_override 数据迁移完成");
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 迁移 bead_brand_color_override 失败: {}", e.getMessage());
        }
    }

    private void seedBeadDataFromSqlFile() {
        if (!seedEnabled) {
            log.info("[SchemaUpgrader] seed is disabled, skip bead_color_init");
            return;
        }

        Integer colorCount = jdbc.queryForObject("SELECT COUNT(*) FROM bead_color", Integer.class);
        if (colorCount != null && colorCount > 0) return;

        try {
            var resource = new ClassPathResource("bead_color_init.sql");
            try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--")) continue;
                    sql.append(line).append('\n');
                }
                String[] statements = sql.toString().split(";\\s*");
                int executed = 0;
                for (String stmt : statements) {
                    String s = stmt.trim();
                    if (s.isEmpty()) continue;
                    try {
                        jdbc.execute(s);
                        executed++;
                    } catch (Exception ex) {
                        String msg = ex.getMessage();
                        if (msg != null && msg.contains("Duplicate entry")) {
                            continue;
                        }
                        throw ex;
                    }
                }
                log.info("[SchemaUpgrader] bead色卡SQL执行完成，语句数: {}", executed);
            }
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 导入bead色卡数据失败: {}", e.getMessage());
        }
    }

    private void seedData() {
        Integer bannerCount = jdbc.queryForObject("SELECT COUNT(*) FROM bp_banner", Integer.class);
        if (bannerCount == null || bannerCount == 0) {
            jdbc.execute("INSERT INTO `bp_banner` (`title`, `sub_title`, `tag_text`, `image_url`, `link_type`, `sort_order`, `status`) VALUES ('初夏限定拼豆', '一键生成专属图纸', '魔法上新', '', 'NONE', 1, 1)");
            log.info("[SchemaUpgrader] 已插入默认Banner");
        }

        Integer planCount = jdbc.queryForObject("SELECT COUNT(*) FROM bp_recharge_plan", Integer.class);
        if (planCount == null || planCount == 0) {
            jdbc.execute("INSERT INTO `bp_recharge_plan` (`name`, `description`, `coins`, `ai_quota`, `price`, `original_price`, `is_vip`, `vip_days`, `tag`, `sort_order`, `status`) VALUES ('体验包', '3次AI生成', 0, 3, 6.00, 9.00, 0, 0, '新人专享', 1, 1),('标准包', '10次AI生成 + 100金币', 100, 10, 18.00, 25.00, 0, 0, '推荐', 2, 1),('豪华包', '30次AI生成 + 500金币', 500, 30, 45.00, 60.00, 0, 0, '超值', 3, 1),('VIP月卡', '无限AI + 1000金币 + VIP', 1000, 999, 28.00, 39.00, 1, 30, '热门', 4, 1)");
            log.info("[SchemaUpgrader] 已插入默认充值套餐");
        }

        Integer adminCount = jdbc.queryForObject("SELECT COUNT(*) FROM bp_admin", Integer.class);
        if (adminCount == null || adminCount == 0) {
            jdbc.execute("INSERT INTO `bp_admin` (`username`, `password`, `nick_name`, `role`, `status`) VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTTyU9iBmi2', '超级管理员', 'SUPER_ADMIN', 1)");
            log.info("[SchemaUpgrader] 已插入默认管理员");
        }
    }

    private void addColumn(String db, String table, String column, String alterSql) {
        try {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=? AND TABLE_NAME=? AND COLUMN_NAME=?",
                    Integer.class, db, table, column
            );
            if (count == null || count == 0) {
                jdbc.execute(alterSql);
                log.info("[SchemaUpgrader] 已添加字段: {}.{}", table, column);
            }
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 添加字段 {}.{} 失败: {}", table, column, e.getMessage());
        }
    }

    private void enforceUserProfileRequired() {
        try {
            jdbc.execute("UPDATE bp_user SET nick_name = '魔法师小豆' WHERE nick_name IS NULL OR TRIM(nick_name) = ''");
            jdbc.execute("UPDATE bp_user SET avatar_url = 'https://dummyimage.com/200x200/ffe9c2/8b5e3c.png&text=%E8%B1%86' WHERE avatar_url IS NULL OR TRIM(avatar_url) = ''");
            jdbc.execute("ALTER TABLE bp_user MODIFY COLUMN nick_name VARCHAR(64) NOT NULL");
            jdbc.execute("ALTER TABLE bp_user MODIFY COLUMN avatar_url VARCHAR(1024) NOT NULL");
            log.info("[SchemaUpgrader] 已完成 bp_user 昵称头像非空约束");
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 设置 bp_user 昵称头像非空约束失败: {}", e.getMessage());
        }
    }

    private void enforceBannerUtf8mb4() {
        try {
            String db = jdbc.queryForObject("SELECT DATABASE()", String.class);
            if (db == null || db.isBlank()) {
                log.warn("[SchemaUpgrader] 获取数据库名失败，跳过 bp_banner 字符集检查");
                return;
            }
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA=? AND TABLE_NAME='bp_banner'",
                    Integer.class, db
            );
            if (count == null || count == 0) {
                log.info("[SchemaUpgrader] bp_banner 表不存在，跳过字符集修复");
                return;
            }
            jdbc.execute("ALTER TABLE `bp_banner` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            log.info("[SchemaUpgrader] 已确保 bp_banner 使用 utf8mb4 字符集");
        } catch (Exception e) {
            log.warn("[SchemaUpgrader] 设置 bp_banner utf8mb4 失败: {}", e.getMessage());
        }
    }
}
