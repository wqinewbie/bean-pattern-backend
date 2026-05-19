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
    @Insert("INSERT INTO bp_ai_generate_task (task_id, user_id, image_url, prompt, style, size_mode, brand, color_count, mirror, " +
            "status, created_at, updated_at) " +
            "VALUES (#{taskId}, #{userId}, #{imageUrl}, #{prompt}, #{style}, #{sizeMode}, #{brand}, #{colorCount}, #{mirror}, " +
            "#{status}, #{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiGenerateTask task);

    /**
     * 根据taskId查询
     */
    @Select("SELECT id, task_id AS taskId, user_id AS userId, image_url AS imageUrl, prompt, style, " +
            "size_mode AS sizeMode, brand, color_count AS colorCount, mirror, " +
            "status, ai_image_url AS aiImageUrl, error_message AS errorMessage, " +
            "completed_at AS completedAt, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_ai_generate_task WHERE task_id = #{taskId}")
    AiGenerateTask findByTaskId(String taskId);

    /**
     * 更新任务
     */
    @Update("UPDATE bp_ai_generate_task SET " +
            "status = #{status}, " +
            "ai_image_url = #{aiImageUrl}, " +
            "error_message = #{errorMessage}, " +
            "completed_at = #{completedAt}, " +
            "updated_at = #{updatedAt} " +
            "WHERE id = #{id}")
    int updateById(AiGenerateTask task);

    /**
     * 根据用户ID查询任务列表
     */
    @Select("SELECT id, task_id AS taskId, user_id AS userId, image_url AS imageUrl, prompt, style, " +
            "size_mode AS sizeMode, brand, color_count AS colorCount, mirror, " +
            "status, ai_image_url AS aiImageUrl, error_message AS errorMessage, " +
            "completed_at AS completedAt, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_ai_generate_task WHERE user_id = #{userId} ORDER BY created_at DESC")
    java.util.List<AiGenerateTask> findByUserId(Long userId);

    @Delete("DELETE FROM bp_ai_generate_task WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
