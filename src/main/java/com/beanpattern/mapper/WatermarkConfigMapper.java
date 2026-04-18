package com.beanpattern.mapper;

import com.beanpattern.entity.WatermarkConfig;
import org.apache.ibatis.annotations.*;

@Mapper
public interface WatermarkConfigMapper {

    @Select("SELECT * FROM bp_watermark_config LIMIT 1")
    WatermarkConfig getConfig();

    @Insert("""
            INSERT INTO bp_watermark_config (enabled, text, font_size, color, position, opacity, margin)
            VALUES (#{enabled}, #{text}, #{fontSize}, #{color}, #{position}, #{opacity}, #{margin})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WatermarkConfig config);

    @Update("""
            UPDATE bp_watermark_config 
            SET enabled=#{enabled}, text=#{text}, font_size=#{fontSize}, color=#{color},
                position=#{position}, opacity=#{opacity}, margin=#{margin}, updated_at=CURRENT_TIMESTAMP
            WHERE id=#{id}
            """)
    int update(WatermarkConfig config);

    @Select("SELECT COUNT(*) FROM bp_watermark_config")
    int count();
}
