package com.beanpattern.mapper;

import com.beanpattern.entity.BpBox;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BpBoxMapper {

    @Insert("""
            INSERT INTO bp_box (user_id, source_type, brand, color_count, name, grid_size,
                              rgb_data, grid_data, color_palette, draft_id, source_url, status, progress_data)
            VALUES (#{userId}, #{sourceType}, #{brand}, #{colorCount}, #{name}, #{gridSize},
                   #{rgbData}, #{gridData}, #{colorPalette}, #{draftId}, #{sourceUrl}, #{status}, #{progressData})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BpBox box);

    @Select("SELECT id, user_id, source_type, brand, color_count, name, grid_size, "
          + "rgb_data, grid_data, color_palette, draft_id, history_id, source_url, status, progress_data, "
          + "created_at, updated_at FROM bp_box WHERE id=#{id}")
    BpBox findById(@Param("id") Long id);

    @Update("""
            UPDATE bp_box SET name=#{name}, grid_data=#{gridData}, color_palette=#{colorPalette},
                             rgb_data=#{rgbData}, status=#{status}, progress_data=#{progressData}, updated_at=CURRENT_TIMESTAMP
            WHERE id=#{id}
            """)
    int update(BpBox box);

    @Delete("DELETE FROM bp_box WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT id, user_id, source_type, brand, color_count, name, grid_size, "
          + "rgb_data, grid_data, color_palette, draft_id, history_id, source_url, status, progress_data, "
          + "created_at, updated_at FROM bp_box WHERE user_id=#{userId} ORDER BY created_at DESC")
    List<BpBox> listByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id, source_type, brand, color_count, name, grid_size, "
          + "rgb_data, grid_data, color_palette, draft_id, history_id, source_url, status, progress_data, "
          + "created_at, updated_at FROM bp_box WHERE user_id=#{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<BpBox> listByUserIdWithLimit(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM bp_box WHERE user_id=#{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Update("UPDATE bp_box SET box_id=#{boxId}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int linkBoxId(@Param("id") Long id, @Param("boxId") Long boxId);
}
