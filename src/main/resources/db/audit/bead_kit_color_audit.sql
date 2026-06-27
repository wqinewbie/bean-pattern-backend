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

-- 2. Placeholder or suspicious colors currently included in kits.
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
WHERE c.code LIKE 'VT%' OR c.hex = '#000000'
ORDER BY b.id, k.color_count, c.code;

-- 3. Legacy palette duplicates that were collapsed by bead_brand_kit_color.
SELECT
  b.name AS brand_name,
  k.color_count,
  c.code,
  COUNT(*) AS legacy_hits,
  GROUP_CONCAT(p.name ORDER BY p.name SEPARATOR ',') AS legacy_palettes
FROM bead_brand_kit k
JOIN bead_brand b ON b.id = k.brand_id
JOIN bead_brand_kit_palette bkp ON bkp.kit_id = k.id
JOIN bead_palette p ON p.id = bkp.palette_id
JOIN bead_palette_color pc ON pc.palette_id = p.id
JOIN bead_color c ON c.id = pc.color_id
GROUP BY b.name, k.color_count, c.code
HAVING legacy_hits > 1
ORDER BY b.name, k.color_count, c.code;
