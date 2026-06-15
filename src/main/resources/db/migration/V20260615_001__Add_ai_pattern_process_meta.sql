ALTER TABLE bp_ai_generate_task
    ADD COLUMN selected_image_variant VARCHAR(32) NULL COMMENT 'Selected AI image variant for pattern processing: RAW or REFINED' AFTER perfect_pixel_error,
    ADD COLUMN process_meta JSON NULL COMMENT 'AI pattern processing candidate scores and selection metadata' AFTER selected_image_variant;
