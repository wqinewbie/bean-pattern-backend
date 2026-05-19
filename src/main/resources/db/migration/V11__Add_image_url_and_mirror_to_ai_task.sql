ALTER TABLE bp_ai_generate_task
  ADD COLUMN image_url VARCHAR(1024) NULL COMMENT '用户上传的原图URL',
  ADD COLUMN mirror TINYINT(1) DEFAULT 0 COMMENT '镜像开关',
  CHANGE COLUMN size size_mode VARCHAR(16) NOT NULL DEFAULT 'default' COMMENT '尺寸模式: default/small';
