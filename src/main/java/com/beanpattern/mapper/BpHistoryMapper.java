package com.beanpattern.mapper;

import com.beanpattern.entity.BpHistory;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BpHistoryMapper {

    @Insert("""
            INSERT INTO bp_history (user_id, task_id, source_type, brand, color_count, name, grid_size,
                                   box_id, source_url, expires_at, mapped_pixel_data)
            VALUES (#{userId}, #{taskId}, #{sourceType}, #{brand}, #{colorCount}, #{name}, #{gridSize},
                    #{boxId}, #{sourceUrl}, #{expiresAt}, #{mappedPixelData})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BpHistory history);

    @Delete("DELETE FROM bp_history WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT id, user_id AS userId, task_id AS taskId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, source_url AS sourceUrl, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, expires_at AS expiresAt FROM bp_history WHERE id=#{id}")
    BpHistory findById(@Param("id") Long id);

    @Select("SELECT id, user_id AS userId, task_id AS taskId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, source_url AS sourceUrl, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, expires_at AS expiresAt FROM bp_history WHERE user_id=#{userId} AND (expires_at IS NULL OR expires_at > NOW()) ORDER BY created_at DESC")
    List<BpHistory> listByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, task_id AS taskId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, source_url AS sourceUrl, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, expires_at AS expiresAt FROM bp_history WHERE user_id=#{userId} AND (expires_at IS NULL OR expires_at > NOW()) ORDER BY created_at DESC LIMIT #{limit}")
    List<BpHistory> listByUserIdWithLimit(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT id, user_id AS userId, task_id AS taskId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, source_url AS sourceUrl, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, expires_at AS expiresAt FROM bp_history WHERE user_id=#{userId} AND (expires_at IS NULL OR expires_at > NOW()) ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<BpHistory> listByUserIdWithPage(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM bp_history WHERE user_id=#{userId} AND (expires_at IS NULL OR expires_at > NOW())")
    int countByUserId(@Param("userId") Long userId);

    @Update("UPDATE bp_history SET box_id=#{boxId} WHERE id=#{id}")
    int linkBoxId(@Param("id") Long id, @Param("boxId") Long boxId);

    @Update("UPDATE bp_history SET box_id=NULL WHERE box_id=#{boxId}")
    int clearBoxId(@Param("boxId") Long boxId);

    @Select("SELECT id, user_id AS userId, task_id AS taskId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "box_id AS boxId, source_url AS sourceUrl, mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, expires_at AS expiresAt FROM bp_history WHERE expires_at IS NOT NULL AND expires_at < NOW()")
    List<BpHistory> listExpired();

    @Delete("DELETE FROM bp_history WHERE expires_at IS NOT NULL AND expires_at < NOW()")
    int deleteExpired();

    // 管理后台接口
    @Select("SELECT * FROM bp_history WHERE (expires_at IS NULL OR expires_at > NOW()) ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
        @Result(property = "userId", column = "user_id"),
        @Result(property = "taskId", column = "task_id"),
        @Result(property = "sourceType", column = "source_type"),
        @Result(property = "colorCount", column = "color_count"),
        @Result(property = "gridSize", column = "grid_size"),
        @Result(property = "boxId", column = "box_id"),
        @Result(property = "sourceUrl", column = "source_url"),
        @Result(property = "mappedPixelData", column = "mapped_pixel_data"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "expiresAt", column = "expires_at")
    })
    List<BpHistory> listAllWithPage(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM bp_history WHERE (expires_at IS NULL OR expires_at > NOW())")
    int countAll();

    @Delete("DELETE FROM bp_history WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
