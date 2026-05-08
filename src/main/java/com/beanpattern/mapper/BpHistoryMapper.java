package com.beanpattern.mapper;

import com.beanpattern.entity.BpHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BpHistoryMapper {

    @Insert("""
            INSERT INTO bp_history (user_id, source_type, brand, color_count, name, grid_size,
                                   box_id, source_url, expires_at, mapped_pixel_data)
            VALUES (#{userId}, #{sourceType}, #{brand}, #{colorCount}, #{name}, #{gridSize},
                    #{boxId}, #{sourceUrl}, #{expiresAt}, #{mappedPixelData})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BpHistory history);

    @Delete("DELETE FROM bp_history WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, source_url AS sourceUrl, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, expires_at AS expiresAt FROM bp_history WHERE id=#{id}")
    BpHistory findById(@Param("id") Long id);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, source_url AS sourceUrl, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, expires_at AS expiresAt FROM bp_history WHERE user_id=#{userId} ORDER BY created_at DESC")
    List<BpHistory> listByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, source_url AS sourceUrl, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, expires_at AS expiresAt FROM bp_history WHERE user_id=#{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<BpHistory> listByUserIdWithLimit(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, source_url AS sourceUrl, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, expires_at AS expiresAt FROM bp_history WHERE user_id=#{userId} ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<BpHistory> listByUserIdWithPage(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM bp_history WHERE user_id=#{userId}")
    int countByUserId(@Param("userId") Long userId);

    @Update("UPDATE bp_history SET box_id=#{boxId} WHERE id=#{id}")
    int linkBoxId(@Param("id") Long id, @Param("boxId") Long boxId);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, source_url AS sourceUrl, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, expires_at AS expiresAt FROM bp_history WHERE expires_at < NOW()")
    List<BpHistory> listExpired();

    @Delete("DELETE FROM bp_history WHERE expires_at < NOW()")
    int deleteExpired();
}
