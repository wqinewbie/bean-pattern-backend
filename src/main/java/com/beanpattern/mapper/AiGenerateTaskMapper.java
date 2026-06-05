package com.beanpattern.mapper;

import com.beanpattern.entity.AiGenerateTask;
import org.apache.ibatis.annotations.*;

/**
 * AI生成任务Mapper
 */
@Mapper
public interface AiGenerateTaskMapper {

    /**
     * 插入任务
     */
    @Insert("INSERT INTO bp_ai_generate_task (task_id, user_id, image_url, prompt, style, size_mode, grid_min, grid_max, brand, color_count, mirror, " +
            "status, mapped_pixel_data, history_id, created_at, updated_at) " +
            "VALUES (#{taskId}, #{userId}, #{imageUrl}, #{prompt}, #{style}, #{sizeMode}, #{gridMin}, #{gridMax}, #{brand}, #{colorCount}, #{mirror}, " +
            "#{status}, #{mappedPixelData}, #{historyId}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiGenerateTask task);

    /**
     * 根据taskId查询
     */
    @Select("SELECT id, task_id AS taskId, user_id AS userId, image_url AS imageUrl, prompt, style, " +
            "size_mode AS sizeMode, grid_min AS gridMin, grid_max AS gridMax, brand, color_count AS colorCount, mirror, " +
            "status, ai_image_url AS aiImageUrl, ai_image_key AS aiImageKey, raw_ai_image_url AS rawAiImageUrl, raw_ai_image_key AS rawAiImageKey, " +
            "detected_grid_width AS detectedGridWidth, detected_grid_height AS detectedGridHeight, " +
            "final_grid_width AS finalGridWidth, final_grid_height AS finalGridHeight, " +
            "perfect_pixel_status AS perfectPixelStatus, perfect_pixel_error AS perfectPixelError, error_message AS errorMessage, " +
            "mapped_pixel_data AS mappedPixelData, history_id AS historyId, " +
            "completed_at AS completedAt, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_ai_generate_task WHERE task_id = #{taskId}")
    AiGenerateTask findByTaskId(String taskId);

    @Select("SELECT id, task_id AS taskId, user_id AS userId, image_url AS imageUrl, prompt, style, " +
            "size_mode AS sizeMode, grid_min AS gridMin, grid_max AS gridMax, brand, color_count AS colorCount, mirror, " +
            "status, ai_image_url AS aiImageUrl, ai_image_key AS aiImageKey, raw_ai_image_url AS rawAiImageUrl, raw_ai_image_key AS rawAiImageKey, " +
            "detected_grid_width AS detectedGridWidth, detected_grid_height AS detectedGridHeight, " +
            "final_grid_width AS finalGridWidth, final_grid_height AS finalGridHeight, " +
            "perfect_pixel_status AS perfectPixelStatus, perfect_pixel_error AS perfectPixelError, error_message AS errorMessage, " +
            "mapped_pixel_data AS mappedPixelData, history_id AS historyId, " +
            "completed_at AS completedAt, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_ai_generate_task WHERE history_id = #{historyId} ORDER BY updated_at DESC LIMIT 1")
    AiGenerateTask findByHistoryId(@Param("historyId") Long historyId);

    /**
     * 更新任务
     */
    @Update("UPDATE bp_ai_generate_task SET " +
            "status = #{status}, " +
            "ai_image_url = #{aiImageUrl}, " +
            "ai_image_key = #{aiImageKey}, " +
            "raw_ai_image_url = #{rawAiImageUrl}, " +
            "raw_ai_image_key = #{rawAiImageKey}, " +
            "detected_grid_width = #{detectedGridWidth}, " +
            "detected_grid_height = #{detectedGridHeight}, " +
            "final_grid_width = #{finalGridWidth}, " +
            "final_grid_height = #{finalGridHeight}, " +
            "perfect_pixel_status = #{perfectPixelStatus}, " +
            "perfect_pixel_error = #{perfectPixelError}, " +
            "error_message = #{errorMessage}, " +
            "mapped_pixel_data = #{mappedPixelData}, " +
            "history_id = #{historyId}, " +
            "completed_at = #{completedAt}, " +
            "updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int updateById(AiGenerateTask task);

    /**
     * 根据用户ID查询任务列表
     */
    @Select("SELECT id, task_id AS taskId, user_id AS userId, image_url AS imageUrl, prompt, style, " +
            "size_mode AS sizeMode, grid_min AS gridMin, grid_max AS gridMax, brand, color_count AS colorCount, mirror, " +
            "status, ai_image_url AS aiImageUrl, ai_image_key AS aiImageKey, raw_ai_image_url AS rawAiImageUrl, raw_ai_image_key AS rawAiImageKey, " +
            "detected_grid_width AS detectedGridWidth, detected_grid_height AS detectedGridHeight, " +
            "final_grid_width AS finalGridWidth, final_grid_height AS finalGridHeight, " +
            "perfect_pixel_status AS perfectPixelStatus, perfect_pixel_error AS perfectPixelError, error_message AS errorMessage, " +
            "mapped_pixel_data AS mappedPixelData, history_id AS historyId, " +
            "completed_at AS completedAt, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_ai_generate_task WHERE user_id = #{userId} ORDER BY created_at DESC")
    java.util.List<AiGenerateTask> findByUserId(Long userId);

    @Select("SELECT id, task_id AS taskId, user_id AS userId, image_url AS imageUrl, prompt, style, " +
            "size_mode AS sizeMode, grid_min AS gridMin, grid_max AS gridMax, brand, color_count AS colorCount, mirror, " +
            "status, ai_image_url AS aiImageUrl, ai_image_key AS aiImageKey, raw_ai_image_url AS rawAiImageUrl, raw_ai_image_key AS rawAiImageKey, " +
            "detected_grid_width AS detectedGridWidth, detected_grid_height AS detectedGridHeight, " +
            "final_grid_width AS finalGridWidth, final_grid_height AS finalGridHeight, " +
            "perfect_pixel_status AS perfectPixelStatus, perfect_pixel_error AS perfectPixelError, error_message AS errorMessage, " +
            "mapped_pixel_data AS mappedPixelData, history_id AS historyId, " +
            "completed_at AS completedAt, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_ai_generate_task WHERE status IN ('PENDING', 'PROCESSING') " +
            "AND created_at < #{cutoff} ORDER BY created_at ASC LIMIT #{limit}")
    java.util.List<AiGenerateTask> findTimedOutActiveTasks(@Param("cutoff") java.util.Date cutoff,
                                                           @Param("limit") int limit);

    @Delete("DELETE FROM bp_ai_generate_task WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
