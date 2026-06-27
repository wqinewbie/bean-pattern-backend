SET @bead_color_display_name_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'bead_color'
    AND COLUMN_NAME = 'display_name'
);

SET @bead_color_display_name_sql := IF(
  @bead_color_display_name_exists = 0,
  'ALTER TABLE bead_color ADD COLUMN display_name VARCHAR(64) NULL AFTER code',
  'SELECT 1'
);

PREPARE bead_color_display_name_stmt FROM @bead_color_display_name_sql;
EXECUTE bead_color_display_name_stmt;
DEALLOCATE PREPARE bead_color_display_name_stmt;
