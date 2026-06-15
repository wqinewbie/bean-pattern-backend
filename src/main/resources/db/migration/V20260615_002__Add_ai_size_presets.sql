CREATE TABLE IF NOT EXISTS bp_ai_size_preset (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  preset_key VARCHAR(32) NOT NULL,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(128) NULL,
  grid_min INT NOT NULL,
  grid_max INT NOT NULL,
  candidate_grids VARCHAR(512) NOT NULL,
  default_grid INT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  recommended TINYINT(1) NOT NULL DEFAULT 0,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  remark VARCHAR(256) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ai_size_preset_key(preset_key),
  KEY idx_ai_size_preset_enabled_sort(enabled, sort_order)
) DEFAULT CHARSET=utf8mb4 COMMENT='AI size preset config';

INSERT INTO bp_ai_size_preset
  (preset_key, name, description, grid_min, grid_max, candidate_grids, default_grid, sort_order, recommended, enabled, remark)
VALUES
  ('small', '小图', '40格以内', 24, 40, '[24,28,32,36,40]', 32, 10, 0, 1, '适合头像、小物件和快速生成'),
  ('standard', '标准', '80格以内', 32, 80, '[32,36,40,44,48,56,64,72,80]', 64, 20, 1, 1, '默认档位，兼顾细节和制作成本'),
  ('detailed', '精细', '104格以内', 48, 104, '[48,56,64,72,80,88,96,104]', 96, 30, 0, 1, '适合更复杂主体和精细图纸')
ON DUPLICATE KEY UPDATE
  name = VALUES(name),
  description = VALUES(description),
  grid_min = VALUES(grid_min),
  grid_max = VALUES(grid_max),
  candidate_grids = VALUES(candidate_grids),
  default_grid = VALUES(default_grid),
  sort_order = VALUES(sort_order),
  recommended = VALUES(recommended),
  enabled = VALUES(enabled),
  remark = VALUES(remark);

ALTER TABLE bp_ai_generate_task
  ADD COLUMN size_preset VARCHAR(32) NULL COMMENT 'AI size preset key snapshot' AFTER size_mode,
  ADD COLUMN size_preset_name VARCHAR(64) NULL COMMENT 'AI size preset name snapshot' AFTER size_preset,
  ADD COLUMN candidate_grids VARCHAR(512) NULL COMMENT 'AI size candidate grids snapshot' AFTER grid_max,
  ADD COLUMN default_grid INT NULL COMMENT 'AI size default grid snapshot' AFTER candidate_grids;
