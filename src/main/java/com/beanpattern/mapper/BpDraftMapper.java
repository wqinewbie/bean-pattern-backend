package com.beanpattern.mapper;

import com.beanpattern.entity.BpDraft;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BpDraftMapper {

    @Insert("""
            INSERT INTO bp_draft (user_id, source_type, brand, color_count, name, grid_size,
                                 rgb_data, grid_data, color_palette, box_id, expires_at,
                                 pixel_data, color_mapping)
            VALUES (#{userId}, #{sourceType}, #{brand}, #{colorCount}, #{name}, #{gridSize},
                    #{rgbData}, #{gridData}, #{colorPalette}, #{boxId}, #{expiresAt},
                    #{pixelData}, #{colorMapping})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BpDraft draft);

    @Update("""
            UPDATE bp_draft SET name=#{name}, grid_data=#{gridData}, color_palette=#{colorPalette},
                               rgb_data=#{rgbData}, pixel_data=#{pixelData}, color_mapping=#{colorMapping},
                               updated_at=CURRENT_TIMESTAMP
            WHERE id=#{id}
            """)
    int update(BpDraft draft);

    @Delete("DELETE FROM bp_draft WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "rgb_data AS rgbData, grid_data AS gridData, color_palette AS colorPalette, box_id AS boxId, "
          + "pixel_data AS pixelData, color_mapping AS colorMapping, "
          + "created_at AS createdAt, updated_at AS updatedAt, expires_at AS expiresAt FROM bp_draft WHERE id=#{id}")
    BpDraft findById(@Param("id") Long id);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "rgb_data AS rgbData, grid_data AS gridData, color_palette AS colorPalette, box_id AS boxId, "
          + "pixel_data AS pixelData, color_mapping AS colorMapping, "
          + "created_at AS createdAt, updated_at AS updatedAt, expires_at AS expiresAt FROM bp_draft WHERE user_id=#{userId} ORDER BY updated_at DESC")
    List<BpDraft> listByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "rgb_data AS rgbData, grid_data AS gridData, color_palette AS colorPalette, box_id AS boxId, "
          + "pixel_data AS pixelData, color_mapping AS colorMapping, "
          + "created_at AS createdAt, updated_at AS updatedAt, expires_at AS expiresAt FROM bp_draft WHERE user_id=#{userId} ORDER BY updated_at DESC LIMIT #{limit}")
    List<BpDraft> listByUserIdWithLimit(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM bp_draft WHERE user_id=#{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Update("UPDATE bp_draft SET box_id=#{boxId}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int linkBoxId(@Param("id") Long id, @Param("boxId") Long boxId);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "rgb_data AS rgbData, grid_data AS gridData, color_palette AS colorPalette, box_id AS boxId, "
          + "pixel_data AS pixelData, color_mapping AS colorMapping, "
          + "created_at AS createdAt, updated_at AS updatedAt, expires_at AS expiresAt FROM bp_draft WHERE expires_at < NOW()")
    List<BpDraft> listExpired();

    @Delete("DELETE FROM bp_draft WHERE expires_at < NOW()")
    int deleteExpired();
}
