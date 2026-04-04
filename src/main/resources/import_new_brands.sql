-- 新品牌补全 SQL - 根据 PDF 表格生成
-- 品牌: COCO、漫漫、盼盼、咪小窝

-- 1. 添加新品牌
INSERT INTO bead_brand (name) VALUES ('COCO'), ('漫漫'), ('盼盼'), ('咪小窝')
ON DUPLICATE KEY UPDATE name=name;

-- 2. 为新品牌创建色盘
INSERT INTO bead_palette (name, remark) VALUES
  ('1_COCO', 'COCO'), ('2_COCO', 'COCO'), ('3_COCO', 'COCO'), ('4_COCO', 'COCO'),
  ('A_COCO', 'COCO'), ('B_COCO', 'COCO'), ('C_COCO', 'COCO'), ('D_COCO', 'COCO'),
  ('E_COCO', 'COCO'), ('6_COCO', 'COCO'), ('7_COCO', 'COCO'), ('8_COCO', 'COCO'),
  ('9_COCO', 'COCO'), ('10_COCO', 'COCO'), ('11_COCO', 'COCO'), ('12_COCO', 'COCO'),
  ('1_漫漫', '漫漫'), ('2_漫漫', '漫漫'), ('3_漫漫', '漫漫'), ('4_漫漫', '漫漫'),
  ('A_漫漫', '漫漫'), ('B_漫漫', '漫漫'), ('C_漫漫', '漫漫'), ('D_漫漫', '漫漫'),
  ('E_漫漫', '漫漫'), ('6_漫漫', '漫漫'), ('7_漫漫', '漫漫'), ('8_漫漫', '漫漫'),
  ('9_漫漫', '漫漫'), ('10_漫漫', '漫漫'), ('11_漫漫', '漫漫'), ('12_漫漫', '漫漫'),
  ('1_盼盼', '盼盼'), ('2_盼盼', '盼盼'), ('3_盼盼', '盼盼'), ('4_盼盼', '盼盼'),
  ('A_盼盼', '盼盼'), ('B_盼盼', '盼盼'), ('C_盼盼', '盼盼'), ('D_盼盼', '盼盼'),
  ('E_盼盼', '盼盼'), ('6_盼盼', '盼盼'), ('7_盼盼', '盼盼'), ('8_盼盼', '盼盼'),
  ('9_盼盼', '盼盼'), ('10_盼盼', '盼盼'), ('11_盼盼', '盼盼'), ('12_盼盼', '盼盼'),
  ('1_咪小窝', '咪小窝'), ('2_咪小窝', '咪小窝'), ('3_咪小窝', '咪小窝'), ('4_咪小窝', '咪小窝'),
  ('A_咪小窝', '咪小窝'), ('B_咪小窝', '咪小窝'), ('C_咪小窝', '咪小窝'), ('D_咪小窝', '咪小窝'),
  ('E_咪小窝', '咪小窝'), ('6_咪小窝', '咪小窝'), ('7_咪小窝', '咪小窝'), ('8_咪小窝', '咪小窝'),
  ('9_咪小窝', '咪小窝'), ('10_咪小窝', '咪小窝'), ('11_咪小窝', '咪小窝'), ('12_咪小窝', '咪小窝')
ON DUPLICATE KEY UPDATE name=name;

-- 3. 复制 MARD 的色盘色码关联到新品牌
INSERT INTO bead_palette_color (palette_id, color_id)
SELECT (SELECT id FROM bead_palette WHERE name=CONCAT(SUBSTRING_INDEX(bp.name,'_',1),'_COCO')), bpc.color_id
FROM bead_palette_color bpc
JOIN bead_palette bp ON bpc.palette_id = bp.id
WHERE bp.name IN ('1','2','3','4','A','B','C','D','E','6','7','8_MARD','9','10','11')
ON DUPLICATE KEY UPDATE palette_id=palette_id;

INSERT INTO bead_palette_color (palette_id, color_id)
SELECT (SELECT id FROM bead_palette WHERE name=CONCAT(SUBSTRING_INDEX(bp.name,'_',1),'_漫漫')), bpc.color_id
FROM bead_palette_color bpc
JOIN bead_palette bp ON bpc.palette_id = bp.id
WHERE bp.name IN ('1','2','3','4','A','B','C','D','E','6','7','8_MARD','9','10','11')
ON DUPLICATE KEY UPDATE palette_id=palette_id;

INSERT INTO bead_palette_color (palette_id, color_id)
SELECT (SELECT id FROM bead_palette WHERE name=CONCAT(SUBSTRING_INDEX(bp.name,'_',1),'_盼盼')), bpc.color_id
FROM bead_palette_color bpc
JOIN bead_palette bp ON bpc.palette_id = bp.id
WHERE bp.name IN ('1','2','3','4','A','B','C','D','E','6','7','8_MARD','9','10','11')
ON DUPLICATE KEY UPDATE palette_id=palette_id;

INSERT INTO bead_palette_color (palette_id, color_id)
SELECT (SELECT id FROM bead_palette WHERE name=CONCAT(SUBSTRING_INDEX(bp.name,'_',1),'_咪小窝')), bpc.color_id
FROM bead_palette_color bpc
JOIN bead_palette bp ON bpc.palette_id = bp.id
WHERE bp.name IN ('1','2','3','4','A','B','C','D','E','6','7','8_MARD','9','10','11')
ON DUPLICATE KEY UPDATE palette_id=palette_id;

-- 4. 添加品牌套装
INSERT INTO bead_brand_kit (brand_id, color_count, palette_ids) VALUES
  ((SELECT id FROM bead_brand WHERE name='COCO'), 24, '1_COCO'),
  ((SELECT id FROM bead_brand WHERE name='COCO'), 48, '1_COCO,2_COCO'),
  ((SELECT id FROM bead_brand WHERE name='COCO'), 72, '1_COCO,2_COCO,3_COCO'),
  ((SELECT id FROM bead_brand WHERE name='COCO'), 96, '1_COCO,2_COCO,3_COCO,4_COCO'),
  ((SELECT id FROM bead_brand WHERE name='COCO'), 120, 'A_COCO,B_COCO,C_COCO,D_COCO,E_COCO'),
  ((SELECT id FROM bead_brand WHERE name='COCO'), 144, 'A_COCO,B_COCO,C_COCO,D_COCO,E_COCO,6_COCO'),
  ((SELECT id FROM bead_brand WHERE name='COCO'), 216, 'A_COCO,B_COCO,C_COCO,D_COCO,E_COCO,6_COCO,9_COCO,10_COCO,11_COCO'),
  ((SELECT id FROM bead_brand WHERE name='COCO'), 264, 'A_COCO,B_COCO,C_COCO,D_COCO,E_COCO,6_COCO,7_COCO,8_COCO,9_COCO,10_COCO,11_COCO'),
  ((SELECT id FROM bead_brand WHERE name='漫漫'), 24, '1_漫漫'),
  ((SELECT id FROM bead_brand WHERE name='漫漫'), 48, '1_漫漫,2_漫漫'),
  ((SELECT id FROM bead_brand WHERE name='漫漫'), 72, '1_漫漫,2_漫漫,3_漫漫'),
  ((SELECT id FROM bead_brand WHERE name='漫漫'), 96, '1_漫漫,2_漫漫,3_漫漫,4_漫漫'),
  ((SELECT id FROM bead_brand WHERE name='漫漫'), 120, 'A_漫漫,B_漫漫,C_漫漫,D_漫漫,E_漫漫'),
  ((SELECT id FROM bead_brand WHERE name='漫漫'), 144, 'A_漫漫,B_漫漫,C_漫漫,D_漫漫,E_漫漫,6_漫漫'),
  ((SELECT id FROM bead_brand WHERE name='漫漫'), 216, 'A_漫漫,B_漫漫,C_漫漫,D_漫漫,E_漫漫,6_漫漫,9_漫漫,10_漫漫,11_漫漫'),
  ((SELECT id FROM bead_brand WHERE name='漫漫'), 264, 'A_漫漫,B_漫漫,C_漫漫,D_漫漫,E_漫漫,6_漫漫,7_漫漫,8_漫漫,9_漫漫,10_漫漫,11_漫漫'),
  ((SELECT id FROM bead_brand WHERE name='盼盼'), 24, '1_盼盼'),
  ((SELECT id FROM bead_brand WHERE name='盼盼'), 48, '1_盼盼,2_盼盼'),
  ((SELECT id FROM bead_brand WHERE name='盼盼'), 72, '1_盼盼,2_盼盼,3_盼盼'),
  ((SELECT id FROM bead_brand WHERE name='盼盼'), 96, '1_盼盼,2_盼盼,3_盼盼,4_盼盼'),
  ((SELECT id FROM bead_brand WHERE name='盼盼'), 120, 'A_盼盼,B_盼盼,C_盼盼,D_盼盼,E_盼盼'),
  ((SELECT id FROM bead_brand WHERE name='盼盼'), 144, 'A_盼盼,B_盼盼,C_盼盼,D_盼盼,E_盼盼,6_盼盼'),
  ((SELECT id FROM bead_brand WHERE name='盼盼'), 216, 'A_盼盼,B_盼盼,C_盼盼,D_盼盼,E_盼盼,6_盼盼,9_盼盼,10_盼盼,11_盼盼'),
  ((SELECT id FROM bead_brand WHERE name='盼盼'), 264, 'A_盼盼,B_盼盼,C_盼盼,D_盼盼,E_盼盼,6_盼盼,7_盼盼,8_盼盼,9_盼盼,10_盼盼,11_盼盼'),
  ((SELECT id FROM bead_brand WHERE name='咪小窝'), 24, '1_咪小窝'),
  ((SELECT id FROM bead_brand WHERE name='咪小窝'), 48, '1_咪小窝,2_咪小窝'),
  ((SELECT id FROM bead_brand WHERE name='咪小窝'), 72, '1_咪小窝,2_咪小窝,3_咪小窝'),
  ((SELECT id FROM bead_brand WHERE name='咪小窝'), 96, '1_咪小窝,2_咪小窝,3_咪小窝,4_咪小窝'),
  ((SELECT id FROM bead_brand WHERE name='咪小窝'), 120, 'A_咪小窝,B_咪小窝,C_咪小窝,D_咪小窝,E_咪小窝'),
  ((SELECT id FROM bead_brand WHERE name='咪小窝'), 144, 'A_咪小窝,B_咪小窝,C_咪小窝,D_咪小窝,E_咪小窝,6_咪小窝'),
  ((SELECT id FROM bead_brand WHERE name='咪小窝'), 216, 'A_咪小窝,B_咪小窝,C_咪小窝,D_咪小窝,E_咪小窝,6_咪小窝,9_咪小窝,10_咪小窝,11_咪小窝'),
  ((SELECT id FROM bead_brand WHERE name='咪小窝'), 264, 'A_咪小窝,B_咪小窝,C_咪小窝,D_咪小窝,E_咪小窝,6_咪小窝,7_咪小窝,8_咪小窝,9_咪小窝,10_咪小窝,11_咪小窝');
