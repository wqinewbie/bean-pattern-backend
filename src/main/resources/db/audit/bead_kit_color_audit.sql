-- Read-only checks for the bead color kit model.
-- Run in the target schema after migrations.

-- 1. Kits whose actual direct color count does not match their label.
SELECT
  b.name AS brand_name,
  k.id AS kit_id,
  k.color_count AS expected_color_count,
  COUNT(DISTINCT bkc.color_id) AS actual_color_count
FROM bead_brand_kit k
JOIN bead_brand b ON b.id = k.brand_id
LEFT JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
GROUP BY b.name, k.id, k.color_count
HAVING actual_color_count <> expected_color_count
ORDER BY b.id, k.color_count;

-- 2. Virtual placeholder colors currently included in kits.
-- H7 is a legitimate black color in the MARD reference data; VT* colors are placeholders.
SELECT DISTINCT
  b.name AS brand_name,
  k.color_count,
  c.code,
  c.hex,
  c.r,
  c.g,
  c.b
FROM bead_brand_kit k
JOIN bead_brand b ON b.id = k.brand_id
JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
JOIN bead_color c ON c.id = bkc.color_id
WHERE c.code LIKE 'VT%'
ORDER BY b.id, k.color_count, c.code;

-- 3. Color rows whose HEX and RGB values disagree.
SELECT
  code,
  display_name,
  hex,
  r,
  g,
  b
FROM bead_color
WHERE hex IS NULL
   OR hex NOT REGEXP '^#[0-9A-Fa-f]{6}$'
   OR UPPER(REPLACE(hex, '#', '')) <> CONCAT(LPAD(HEX(r), 2, '0'), LPAD(HEX(g), 2, '0'), LPAD(HEX(b), 2, '0'))
   OR r NOT BETWEEN 0 AND 255
   OR g NOT BETWEEN 0 AND 255
   OR b NOT BETWEEN 0 AND 255
ORDER BY code;
