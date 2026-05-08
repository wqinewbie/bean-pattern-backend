-- =============================================
-- AI魔法风格配置表
-- =============================================

-- 创建表
CREATE TABLE IF NOT EXISTS ai_magic_style (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  name            VARCHAR(64) NOT NULL COMMENT '风格名称',
  icon            VARCHAR(32) NULL COMMENT '图标emoji',
  category        VARCHAR(32) NOT NULL COMMENT '分类：题材/用途/高阶',
  tag             VARCHAR(64) NULL COMMENT '标签：适用人物/适用宠物等',
  description     VARCHAR(256) NULL COMMENT '风格描述',
  prompt_template VARCHAR(512) NULL COMMENT 'AI提示词模板（正式版使用）',
  sort_order      INT DEFAULT 0 COMMENT '排序',
  enabled         TINYINT DEFAULT 1 COMMENT '0禁用 1启用',
  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  KEY idx_enabled_sort (enabled, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI魔法风格配置表';

-- 插入初始数据
INSERT INTO ai_magic_style (name, icon, category, tag, description, prompt_template, sort_order, enabled) VALUES
('人物特化', '👤', '题材', '适用人物', '专注人物细节，适合人像照片', 'portrait, detailed face, high quality, professional photography', 1, 1),
('宠物毛发', '🐱', '题材', '适用宠物', '突出宠物毛发质感', 'cute pet, fluffy fur, detailed texture, adorable, high quality', 2, 1),
('风景写意', '🏞️', '题材', '适用风景', '风景照片艺术化处理', 'landscape, scenic view, artistic, beautiful nature, high quality', 3, 1),
('卡通二次元', '🎨', '题材', '适用二次元', '动漫卡通风格', 'anime style, cartoon, vibrant colors, cute character, high quality', 4, 1),
('细节保留', '✨', '用途', '通用', '保留更多细节', 'high detail, sharp focus, clear, professional, ultra detailed', 5, 1),
('特征提取', '🎯', '用途', '抓重点', '提取关键特征', 'simplified, key features, minimalist style, clean design', 6, 1),
('大头照', '🖼️', '用途', '无身体', '只保留头部特写', 'close-up portrait, face focus, headshot, no body, professional', 7, 1),
('飞天小女警', '💫', '高阶', '画风融入', '飞天小女警画风', 'powerpuff girls style, cartoon character, cute, colorful', 8, 1),
('迪士尼风', '🏰', '高阶', '画风融入', '迪士尼动画风格', 'disney animation style, magical, colorful, family friendly', 9, 1);
