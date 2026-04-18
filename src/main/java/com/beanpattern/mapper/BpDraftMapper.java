package com.beanpattern.mapper;

import com.beanpattern.entity.BpDraft;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BpDraftMapper {

    @Insert("""
            INSERT INTO bp_draft (user_id, source_type, brand, color_count, name, grid_size,
                                 rgb_data, grid_data, color_palette, box_id, expires_at)
            VALUES (#{userId}, #{sourceType}, #{brand}, #{colorCount}, #{name}, #{gridSize},
                    #{rgbData}, #{gridData}, #{colorPalette}, #{boxId}, #{expiresAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BpDraft draft);

    @Update("""
            UPDATE bp_draft SET name=#{name}, grid_data=#{gridData}, color_palette=#{colorPalette},
                               rgb_data=#{rgbData}, updated_at=CURRENT_TIMESTAMP
            WHERE id=#{id}
            """)
    int update(BpDraft draft);

    @Delete("DELETE FROM bp_draft WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT * FROM bp_draft WHERE id=#{id}")
    BpDraft findById(@Param("id") Long id);

    @Select("SELECT * FROM bp_draft WHERE user_id=#{userId} ORDER BY updated_at DESC")
    List<BpDraft> listByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM bp_draft WHERE user_id=#{userId} ORDER BY updated_at DESC LIMIT #{limit}")
    List<BpDraft> listByUserIdWithLimit(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT COUNT(*) FROM bp_draft WHERE user_id=#{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Update("UPDATE bp_draft SET box_id=#{boxId}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int linkBoxId(@Param("id") Long id, @Param("boxId") Long boxId);

    @Select("SELECT * FROM bp_draft WHERE expires_at < NOW()")
    List<BpDraft> listExpired();

    @Delete("DELETE FROM bp_draft WHERE expires_at < NOW()")
    int deleteExpired();
}
