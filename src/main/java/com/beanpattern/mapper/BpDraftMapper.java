package com.beanpattern.mapper;

import com.beanpattern.entity.BpDraft;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BpDraftMapper {

    @Insert("""
            INSERT INTO bp_draft (user_id, source_type, brand, color_count, name, grid_size,
                                 box_id, expires_at, mapped_pixel_data)
            VALUES (#{userId}, #{sourceType}, #{brand}, #{colorCount}, #{name}, #{gridSize},
                    #{boxId}, #{expiresAt}, #{mappedPixelData})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BpDraft draft);

    @Update("""
            UPDATE bp_draft SET
                name=#{name},
                brand=#{brand},
                color_count=#{colorCount},
                grid_size=#{gridSize},
                mapped_pixel_data=#{mappedPixelData},
                updated_at=CURRENT_TIMESTAMP
            WHERE id=#{id}
            """)
    int update(BpDraft draft);

    @Delete("DELETE FROM bp_draft WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, updated_at AS updatedAt, expires_at AS expiresAt FROM bp_draft WHERE id=#{id}")
    BpDraft findById(@Param("id") Long id);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, updated_at AS updatedAt, expires_at AS expiresAt FROM bp_draft WHERE user_id=#{userId} AND expires_at > NOW() ORDER BY updated_at DESC")
    List<BpDraft> listByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, updated_at AS updatedAt, expires_at AS expiresAt FROM bp_draft WHERE user_id=#{userId} AND expires_at > NOW() ORDER BY updated_at DESC LIMIT #{limit}")
    List<BpDraft> listByUserIdWithLimit(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, updated_at AS updatedAt, expires_at AS expiresAt FROM bp_draft WHERE user_id=#{userId} AND expires_at > NOW() ORDER BY updated_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<BpDraft> listByUserIdWithPage(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM bp_draft WHERE user_id=#{userId} AND expires_at > NOW()")
    int countByUserId(@Param("userId") Long userId);

    @Update("UPDATE bp_draft SET box_id=#{boxId}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int linkBoxId(@Param("id") Long id, @Param("boxId") Long boxId);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, updated_at AS updatedAt, expires_at AS expiresAt FROM bp_draft WHERE expires_at < NOW()")
    List<BpDraft> listExpired();

    @Delete("DELETE FROM bp_draft WHERE expires_at < NOW()")
    int deleteExpired();
}
