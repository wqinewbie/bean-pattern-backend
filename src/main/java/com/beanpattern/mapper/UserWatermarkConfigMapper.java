package com.beanpattern.mapper;

import com.beanpattern.entity.UserWatermarkConfig;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserWatermarkConfigMapper {

    @Select("SELECT id, user_id AS userId, enabled, custom_text AS customText, created_at AS createdAt, updated_at AS updatedAt FROM bp_user_watermark_config WHERE user_id=#{userId}")
    UserWatermarkConfig getByUserId(Long userId);

    @Insert("""
            INSERT INTO bp_user_watermark_config (user_id, enabled, custom_text)
            VALUES (#{userId}, #{enabled}, #{customText})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserWatermarkConfig config);

    @Update("""
            UPDATE bp_user_watermark_config 
            SET enabled=#{enabled}, custom_text=#{customText}, updated_at=CURRENT_TIMESTAMP
            WHERE user_id=#{userId}
            """)
    int updateByUserId(UserWatermarkConfig config);

    @Delete("DELETE FROM bp_user_watermark_config WHERE user_id=#{userId}")
    int deleteByUserId(Long userId);
}
