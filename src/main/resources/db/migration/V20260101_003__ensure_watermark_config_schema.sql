CREATE TABLE IF NOT EXISTS bp_watermark_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  app_name VARCHAR(128) NOT NULL DEFAULT '拼豆魔法屋',
  default_text VARCHAR(128) NOT NULL DEFAULT '拼豆魔法屋出品',
  font_size INT NOT NULL DEFAULT 24,
  color VARCHAR(64) NOT NULL DEFAULT 'rgba(100,100,100,0.25)',
  angle INT NOT NULL DEFAULT -30,
  spacing_x_ratio DECIMAL(5,2) NOT NULL DEFAULT 0.22,
  spacing_y_ratio DECIMAL(5,2) NOT NULL DEFAULT 0.18,
  opacity DECIMAL(5,2) NOT NULL DEFAULT 0.25,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO bp_watermark_config(app_name, default_text, font_size, color, angle, spacing_x_ratio, spacing_y_ratio, opacity)
SELECT '拼豆魔法屋', '拼豆魔法屋出品', 24, 'rgba(100,100,100,0.25)', -30, 0.22, 0.18, 0.25
WHERE NOT EXISTS (SELECT 1 FROM bp_watermark_config);

CREATE TABLE IF NOT EXISTS bp_user_watermark_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  enabled TINYINT NOT NULL DEFAULT 1,
  custom_text VARCHAR(128) NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
