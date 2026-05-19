package com.beanpattern.mapper;

import com.beanpattern.entity.BpBox;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BpBoxMapper {

    @Insert("""
            INSERT INTO bp_box (user_id, source_type, brand, color_count, name, grid_size,
                              draft_id, history_id, source_url, cover_url, status,
                              focus_progress, focus_completed_cells, focus_total_cells,
                              mapped_pixel_data)
            VALUES (#{userId}, #{sourceType}, #{brand}, #{colorCount}, #{name}, #{gridSize},
                   #{draftId}, #{historyId}, #{sourceUrl}, #{coverUrl}, #{status},
                   #{focusProgress}, #{focusCompletedCells}, #{focusTotalCells},
                   #{mappedPixelData})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BpBox box);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "draft_id AS draftId, history_id AS historyId, source_url AS sourceUrl, cover_url AS coverUrl, status, "
          + "focus_progress AS focusProgress, focus_completed_cells AS focusCompletedCells, focus_total_cells AS focusTotalCells, "
          + "mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, updated_at AS updatedAt FROM bp_box WHERE id=#{id}")
    BpBox findById(@Param("id") Long id);

    @Update("""
            UPDATE bp_box SET
                name=#{name},
                brand=#{brand},
                color_count=#{colorCount},
                grid_size=#{gridSize},
                draft_id=#{draftId},
                history_id=#{historyId},
                source_url=#{sourceUrl},
                cover_url=#{coverUrl},
                status=#{status},
                focus_progress=#{focusProgress},
                focus_completed_cells=#{focusCompletedCells},
                focus_total_cells=#{focusTotalCells},
                mapped_pixel_data=#{mappedPixelData},
                updated_at=CURRENT_TIMESTAMP
            WHERE id=#{id}
            """)
    int update(BpBox box);

    @Update("UPDATE bp_box SET name=#{name}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int updateName(@Param("id") Long id, @Param("name") String name);

    @Update("UPDATE bp_box SET status=3, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int deleteById(@Param("id") Long id);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "draft_id AS draftId, history_id AS historyId, source_url AS sourceUrl, cover_url AS coverUrl, status, "
          + "focus_progress AS focusProgress, focus_completed_cells AS focusCompletedCells, focus_total_cells AS focusTotalCells, "
          + "mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, updated_at AS updatedAt FROM bp_box WHERE user_id=#{userId} AND status!=3 ORDER BY created_at DESC")
    List<BpBox> listByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "draft_id AS draftId, history_id AS historyId, source_url AS sourceUrl, cover_url AS coverUrl, status, "
          + "focus_progress AS focusProgress, focus_completed_cells AS focusCompletedCells, focus_total_cells AS focusTotalCells, "
          + "mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, updated_at AS updatedAt FROM bp_box WHERE user_id=#{userId} AND status!=3 ORDER BY created_at DESC LIMIT #{limit}")
    List<BpBox> listByUserIdWithLimit(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT id, user_id AS userId, source_type AS sourceType, brand, color_count AS colorCount, name, grid_size AS gridSize, "
          + "draft_id AS draftId, history_id AS historyId, source_url AS sourceUrl, cover_url AS coverUrl, status, "
          + "focus_progress AS focusProgress, focus_completed_cells AS focusCompletedCells, focus_total_cells AS focusTotalCells, "
          + "mapped_pixel_data AS mappedPixelData, "
          + "created_at AS createdAt, updated_at AS updatedAt FROM bp_box WHERE user_id=#{userId} AND status!=3 ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<BpBox> listByUserIdWithPage(@Param("userId") Long userId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM bp_box WHERE user_id=#{userId} AND status!=3")
    int countByUserId(@Param("userId") Long userId);

    // 管理后台接口
    @Select("SELECT * FROM bp_box WHERE status!=3 ORDER BY created_at DESC LIMIT #{limit} OFFSET #{offset}")
    @Results({
        @Result(property = "userId", column = "user_id"),
        @Result(property = "sourceType", column = "source_type"),
        @Result(property = "colorCount", column = "color_count"),
        @Result(property = "gridSize", column = "grid_size"),
        @Result(property = "draftId", column = "draft_id"),
        @Result(property = "historyId", column = "history_id"),
        @Result(property = "sourceUrl", column = "source_url"),
        @Result(property = "coverUrl", column = "cover_url"),
        @Result(property = "focusProgress", column = "focus_progress"),
        @Result(property = "focusCompletedCells", column = "focus_completed_cells"),
        @Result(property = "focusTotalCells", column = "focus_total_cells"),
        @Result(property = "mappedPixelData", column = "mapped_pixel_data"),
        @Result(property = "createdAt", column = "created_at"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<BpBox> listAllWithPage(@Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM bp_box WHERE status!=3")
    int countAll();

    @Delete("DELETE FROM bp_box WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
