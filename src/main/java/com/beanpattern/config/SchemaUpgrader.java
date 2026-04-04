package com.beanpattern.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 数据库字段兼容升级器
 * bead 色卡初始化也在此执行
 */
@Component
@Order(1)
public class SchemaUpgrader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaUpgrader.class);
    private final JdbcTemplate jdbc;

    public SchemaUpgrader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        String db = jdbc.queryForObject("SELECT DATABASE()", String.class);
        log.info("[SchemaUpgrader] 开始检查数据库字段兼容性，数据库: {}", db);

        // bp_user
        addColumn(db, "bp_user", "union_id",      "ALTER TABLE `bp_user` ADD COLUMN `union_id` VARCHAR(64) NULL COMMENT 'UnionID' AFTER `open_id`");
        addColumn(db, "bp_user", "phone",         "ALTER TABLE `bp_user` ADD COLUMN `phone` VARCHAR(20) NULL COMMENT '手机号' AFTER `avatar_url`");
        addColumn(db, "bp_user", "gender",        "ALTER TABLE `bp_user` ADD COLUMN `gender` TINYINT(1) NULL DEFAULT 0 COMMENT '0未知 1男 2女' AFTER `phone`");
        addColumn(db, "bp_user", "vip_level",     "ALTER TABLE `bp_user` ADD COLUMN `vip_level` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0普通 1高级' AFTER `gender`");
        addColumn(db, "bp_user", "vip_expire_at", "ALTER TABLE `bp_user` ADD COLUMN `vip_expire_at` DATETIME NULL COMMENT 'VIP到期时间' AFTER `vip_level`");
        addColumn(db, "bp_user", "magic_coins",   "ALTER TABLE `bp_user` ADD COLUMN `magic_coins` INT NOT NULL DEFAULT 0 COMMENT '金币余额' AFTER `vip_expire_at`");
        addColumn(db, "bp_user", "ai_quota",      "ALTER TABLE `bp_user` ADD COLUMN `ai_quota` INT NOT NULL DEFAULT 3 COMMENT 'AI剩余次数' AFTER `magic_coins`");
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
        addColumn(db, "bp_banner", "start_at",   "ALTER TABLE `bp_banner` ADD COLUMN `start_at` DATETIME NULL AFTER `status`");
        addColumn(db, "bp_banner", "end_at",     "ALTER TABLE `bp_banner` ADD COLUMN `end_at` DATETIME NULL AFTER `start_at`");
        addColumn(db, "bp_banner", "updated_at", "ALTER TABLE `bp_banner` ADD COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER `created_at`");

        // bp_recharge_plan
        addColumn(db, "bp_recharge_plan", "is_vip",   "ALTER TABLE `bp_recharge_plan` ADD COLUMN `is_vip` TINYINT(1) NOT NULL DEFAULT 0 AFTER `original_price`");
        addColumn(db, "bp_recharge_plan", "vip_days", "ALTER TABLE `bp_recharge_plan` ADD COLUMN `vip_days` INT NOT NULL DEFAULT 0 AFTER `is_vip`");

        // bp_order
        addColumn(db, "bp_order", "plan_name", "ALTER TABLE `bp_order` ADD COLUMN `plan_name` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '套餐名称快照' AFTER `plan_id`");

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
        seedData();

        // 用户资料完整性约束：昵称与头像不能为空
        enforceUserProfileRequired();

        log.info("[SchemaUpgrader] 字段兼容性检查完成");
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

            seedBeadDataFromSqlFile();
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

    private void seedBeadDataFromSqlFile() {
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
}
