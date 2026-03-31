package com.beanpattern.mapper;

import com.beanpattern.entity.ImageTaskEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 任务记录 Mapper（bp_image_task）
 */
@Mapper
public interface ImageTaskMapper {

    @Insert("""
            INSERT INTO bp_image_task(user_id, task_type, source_url, result_url, status, error_message)
            VALUES(#{userId}, #{taskType}, #{sourceUrl}, #{resultUrl}, #{status}, #{errorMessage})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ImageTaskEntity task);

    @Update("""
            UPDATE bp_image_task
            SET result_url     = #{resultUrl},
                pattern_url    = #{patternUrl},
                color_stats    = #{colorStats},
                status         = #{status},
                error_message  = #{errorMessage},
                updated_at     = CURRENT_TIMESTAMP
            WHERE id = #{id}
            """)
    int updateResult(@Param("id") Long id,
                     @Param("resultUrl") String resultUrl,
                     @Param("patternUrl") String patternUrl,
                     @Param("colorStats") String colorStats,
                     @Param("status") String status,
                     @Param("errorMessage") String errorMessage);

    @Select("""
            SELECT id, user_id AS userId, task_type AS taskType,
                   source_url AS sourceUrl, result_url AS resultUrl,
                   pattern_url AS patternUrl, color_stats AS colorStats,
                   status, error_message AS errorMessage,
                   is_saved AS isSaved,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM bp_image_task
            WHERE id = #{id}
            """)
    ImageTaskEntity findById(@Param("id") Long id);

    @Select("""
            SELECT id, user_id AS userId, task_type AS taskType,
                   source_url AS sourceUrl, result_url AS resultUrl,
                   pattern_url AS patternUrl, color_stats AS colorStats,
                   status, error_message AS errorMessage,
                   is_saved AS isSaved,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM bp_image_task
            WHERE user_id = #{userId}
            ORDER BY created_at DESC
            LIMIT #{limit}
            """)
    List<ImageTaskEntity> listByUser(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("""
            SELECT id, user_id AS userId, task_type AS taskType,
                   source_url AS sourceUrl, result_url AS resultUrl,
                   pattern_url AS patternUrl, color_stats AS colorStats,
                   status, error_message AS errorMessage,
                   is_saved AS isSaved,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM bp_image_task
            WHERE user_id = #{userId}
            ORDER BY created_at DESC
            LIMIT #{size} OFFSET #{offset}
            """)
    List<ImageTaskEntity> listByUserPage(@Param("userId") Long userId,
                                          @Param("offset") int offset,
                                          @Param("size") int size);

    @Select("SELECT COUNT(*) FROM bp_image_task WHERE user_id = #{userId}")
    int countByUser(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM bp_image_task WHERE user_id = #{userId} AND status = 'SUCCESS'")
    int countSuccessByUser(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM bp_image_task WHERE user_id = #{userId} AND task_type = 'BEAD_AI'")
    int countAiByUser(@Param("userId") Long userId);

    @Update("UPDATE bp_image_task SET is_saved = #{isSaved}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateIsSaved(@Param("id") Long id, @Param("isSaved") int isSaved);

    @Select("SELECT COUNT(*) FROM bp_image_task")
    int count();

    @Select("SELECT id, user_id AS userId, task_type AS taskType, source_url AS sourceUrl, result_url AS resultUrl, pattern_url AS patternUrl, status, error_message AS errorMessage, is_saved AS isSaved, created_at AS createdAt, updated_at AS updatedAt FROM bp_image_task ORDER BY created_at DESC LIMIT #{size} OFFSET #{offset}")
    List<ImageTaskEntity> listAll(@Param("offset") int offset, @Param("size") int size);

    @Select("""
            SELECT id, user_id AS userId, task_type AS taskType,
                   source_url AS sourceUrl, result_url AS resultUrl,
                   pattern_url AS patternUrl, color_stats AS colorStats,
                   status, error_message AS errorMessage,
                   is_saved AS isSaved,
                   created_at AS createdAt, updated_at AS updatedAt
            FROM bp_image_task
            WHERE user_id = #{userId} AND is_saved = 1
            ORDER BY updated_at DESC
            LIMIT #{limit}
            """)
    List<ImageTaskEntity> listSavedByUser(@Param("userId") Long userId, @Param("limit") int limit);
}
