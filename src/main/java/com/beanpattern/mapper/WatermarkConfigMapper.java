package com.beanpattern.mapper;

import com.beanpattern.entity.WatermarkConfig;
import org.apache.ibatis.annotations.*;

@Mapper
public interface WatermarkConfigMapper {

    @Select("SELECT id, app_name, default_text, font_size, color, angle, spacing_x_ratio, spacing_y_ratio, opacity, created_at, updated_at FROM bp_watermark_config LIMIT 1")
    WatermarkConfig getConfig();

    @Insert("""
            INSERT INTO bp_watermark_config (app_name, default_text, font_size, color, angle, spacing_x_ratio, spacing_y_ratio, opacity)
            VALUES (#{appName}, #{defaultText}, #{fontSize}, #{color}, #{angle}, #{spacingXRatio}, #{spacingYRatio}, #{opacity})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WatermarkConfig config);

    @Update("""
            UPDATE bp_watermark_config 
            SET app_name=#{appName}, default_text=#{defaultText}, font_size=#{fontSize}, color=#{color},
                angle=#{angle}, spacing_x_ratio=#{spacingXRatio}, spacing_y_ratio=#{spacingYRatio}, 
                opacity=#{opacity}, updated_at=CURRENT_TIMESTAMP
            WHERE id=#{id}
            """)
    int update(WatermarkConfig config);

    @Select("SELECT COUNT(*) FROM bp_watermark_config")
    int count();
}
