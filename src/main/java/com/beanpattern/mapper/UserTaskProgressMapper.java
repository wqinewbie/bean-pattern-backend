package com.beanpattern.mapper;

import com.beanpattern.entity.UserTaskProgress;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户任务进度 Mapper：user_task_progress
 */
@Mapper
public interface UserTaskProgressMapper {

    @Select("SELECT id, user_id AS userId, task_id AS taskId, task_code AS taskCode, " +
            "current_count AS currentCount, target_count AS targetCount, status, " +
            "completed_at AS completedAt, claimed_at AS claimedAt, period_start AS periodStart, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_task_progress WHERE user_id = #{userId} LIMIT 200")
    List<UserTaskProgress> findByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, task_id AS taskId, task_code AS taskCode, " +
            "current_count AS currentCount, target_count AS targetCount, status, " +
            "completed_at AS completedAt, claimed_at AS claimedAt, period_start AS periodStart, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_task_progress WHERE user_id = #{userId} AND task_code = #{taskCode} " +
            "ORDER BY COALESCE(period_start, DATE('1000-01-01')) DESC, id DESC LIMIT 1")
    UserTaskProgress findByUserIdAndTaskCode(@Param("userId") Long userId, @Param("taskCode") String taskCode);

    @Select("<script>" +
            "SELECT id, user_id AS userId, task_id AS taskId, task_code AS taskCode, " +
            "current_count AS currentCount, target_count AS targetCount, status, " +
            "completed_at AS completedAt, claimed_at AS claimedAt, period_start AS periodStart, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_task_progress " +
            "WHERE user_id = #{userId} AND task_code = #{taskCode} " +
            "<choose>" +
            "<when test='periodStart == null'>AND period_start IS NULL</when>" +
            "<otherwise>AND period_start = #{periodStart}</otherwise>" +
            "</choose> " +
            "ORDER BY id DESC LIMIT 1" +
            "</script>")
    UserTaskProgress findByUserAndTaskCodeAndPeriod(@Param("userId") Long userId,
                                                     @Param("taskCode") String taskCode,
                                                     @Param("periodStart") LocalDate periodStart);

    @Select("<script>" +
            "SELECT id, user_id AS userId, task_id AS taskId, task_code AS taskCode, " +
            "current_count AS currentCount, target_count AS targetCount, status, " +
            "completed_at AS completedAt, claimed_at AS claimedAt, period_start AS periodStart, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_task_progress WHERE user_id = #{userId} AND task_id = #{taskId} " +
            "<choose>" +
            "<when test='periodStart == null'>AND period_start IS NULL</when>" +
            "<otherwise>AND period_start = #{periodStart}</otherwise>" +
            "</choose> " +
            "ORDER BY id DESC LIMIT 1" +
            "</script>")
    UserTaskProgress findByUserAndTaskAndPeriod(@Param("userId") Long userId, 
                                                 @Param("taskId") Long taskId, 
                                                 @Param("periodStart") LocalDate periodStart);

    @Insert("INSERT INTO bp_user_task_progress(user_id, task_id, task_code, current_count, target_count, status, period_start) " +
            "VALUES(#{userId}, #{taskId}, #{taskCode}, #{currentCount}, #{targetCount}, #{status}, #{periodStart})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserTaskProgress progress);

    @Update("UPDATE bp_user_task_progress SET current_count = #{currentCount}, status = #{status}, " +
            "completed_at = #{completedAt}, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateProgress(@Param("id") Long id, 
                       @Param("currentCount") Integer currentCount,
                       @Param("status") Integer status,
                       @Param("completedAt") java.time.LocalDateTime completedAt);

    @Update("UPDATE bp_user_task_progress SET status = 2, claimed_at = CURRENT_TIMESTAMP, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int claim(@Param("id") Long id);
    
    @Select("SELECT id, user_id AS userId, task_id AS taskId, task_code AS taskCode, " +
            "current_count AS currentCount, target_count AS targetCount, status, " +
            "completed_at AS completedAt, claimed_at AS claimedAt, period_start AS periodStart, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_task_progress WHERE id = #{id}")
    UserTaskProgress findById(@Param("id") Long id);

    @Delete("DELETE FROM bp_user_task_progress WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
