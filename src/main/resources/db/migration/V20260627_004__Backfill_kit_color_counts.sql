-- Backfill direct kit-color relations so each kit exposes its advertised color count.
-- Preference:
--   1. Brand-specific override colors, when the brand has overrides.
--   2. Global non-placeholder colors for brands without overrides.
INSERT IGNORE INTO bead_brand_kit_color(kit_id, color_id, sort_order)
WITH kit_counts AS (
  SELECT k.id AS kit_id,
         k.brand_id,
         k.color_count,
         COUNT(DISTINCT bkc.color_id) AS actual_count,
         k.color_count - COUNT(DISTINCT bkc.color_id) AS missing_count,
         EXISTS (
           SELECT 1
           FROM bead_brand_color_override existing_override
           WHERE existing_override.brand_id = k.brand_id
         ) AS has_override
  FROM bead_brand_kit k
  LEFT JOIN bead_brand_kit_color bkc ON bkc.kit_id = k.id
  GROUP BY k.id, k.brand_id, k.color_count
  HAVING missing_count > 0
),
candidates AS (
  SELECT kc.kit_id,
         kc.missing_count,
         c.id AS color_id,
         ROW_NUMBER() OVER (
           PARTITION BY kc.kit_id
           ORDER BY
             CASE WHEN bco.id IS NULL THEN 1 ELSE 0 END,
             c.id
         ) AS rn
  FROM kit_counts kc
  JOIN bead_color c
  LEFT JOIN bead_brand_color_override bco
    ON bco.brand_id = kc.brand_id AND bco.color_id = c.id
  WHERE NOT EXISTS (
      SELECT 1
      FROM bead_brand_kit_color existing_color
      WHERE existing_color.kit_id = kc.kit_id AND existing_color.color_id = c.id
    )
    AND (
      (kc.has_override = 1 AND bco.id IS NOT NULL)
      OR
      (kc.has_override = 0 AND c.code NOT LIKE 'VT%' AND c.hex <> '#000000')
    )
)
SELECT kit_id,
       color_id,
       900000 + rn AS sort_order
FROM candidates
WHERE rn <= missing_count;
