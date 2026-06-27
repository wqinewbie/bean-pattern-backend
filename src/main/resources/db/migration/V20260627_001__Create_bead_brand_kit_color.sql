CREATE TABLE IF NOT EXISTS bead_brand_kit_color (
  kit_id INT NOT NULL COMMENT '套装ID',
  color_id INT NOT NULL COMMENT '色号ID',
  sort_order INT NOT NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (kit_id, color_id),
  KEY idx_bkc_color_id (color_id),
  KEY idx_bkc_kit_sort (kit_id, sort_order)
) DEFAULT CHARSET=utf8mb4 COMMENT='品牌套装-色号直接关联表';

INSERT IGNORE INTO bead_brand_kit_color (kit_id, color_id, sort_order)
SELECT
  bkp.kit_id,
  pc.color_id,
  MIN(bkp.sort_order * 1000 + c.id) AS sort_order
FROM bead_brand_kit_palette bkp
JOIN bead_palette_color pc ON pc.palette_id = bkp.palette_id
JOIN bead_color c ON c.id = pc.color_id
GROUP BY bkp.kit_id, pc.color_id;
