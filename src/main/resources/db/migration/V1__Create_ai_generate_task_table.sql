-- AI生成任务表
CREATE TABLE IF NOT EXISTS bp_ai_generate_task (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  task_id         VARCHAR(64) NOT NULL UNIQUE COMMENT '任务ID',
  user_id         BIGINT NOT NULL COMMENT '用户ID',

  -- 请求参数
  prompt          VARCHAR(512) NULL COMMENT '提示词（可选）',
  style           VARCHAR(64) NOT NULL COMMENT '风格',
  size            INT NOT NULL COMMENT '尺寸',
  brand           VARCHAR(32) NOT NULL COMMENT '品牌',
  color_count     INT DEFAULT 0 COMMENT '色数',

  -- 任务状态
  status          VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSING/SUCCESS/FAILED',

  -- 结果数据
  ai_image_url    VARCHAR(1024) NULL COMMENT 'AI生成的图片URL',
  error_message   VARCHAR(512) NULL COMMENT '错误信息',

  -- 时间戳
  created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  completed_at    DATETIME NULL COMMENT '完成时间',

  KEY idx_task_id (task_id),
  KEY idx_user (user_id),
  KEY idx_status (status),
  KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI生成任务表';
