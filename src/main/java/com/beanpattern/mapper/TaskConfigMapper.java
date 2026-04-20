package com.beanpattern.mapper;

import com.beanpattern.entity.TaskConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 任务配置 Mapper：task_config
 */
@Mapper
public interface TaskConfigMapper {

    @Select("SELECT id, task_code AS taskCode, task_name AS taskName, task_type AS taskType, " +
            "description, target_count AS targetCount, reward_type AS rewardType, reward_value AS rewardValue, " +
            "icon_url AS iconUrl, action_url AS actionUrl, sort_order AS sortOrder, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM task_config WHERE status = 1 ORDER BY sort_order ASC")
    List<TaskConfig> findAllActive();

    @Select("SELECT id, task_code AS taskCode, task_name AS taskName, task_type AS taskType, " +
            "description, target_count AS targetCount, reward_type AS rewardType, reward_value AS rewardValue, " +
            "icon_url AS iconUrl, action_url AS actionUrl, sort_order AS sortOrder, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM task_config WHERE task_code = #{taskCode}")
    TaskConfig findByCode(@Param("taskCode") String taskCode);

    @Select("SELECT id, task_code AS taskCode, task_name AS taskName, task_type AS taskType, " +
            "description, target_count AS targetCount, reward_type AS rewardType, reward_value AS rewardValue, " +
            "icon_url AS iconUrl, action_url AS actionUrl, sort_order AS sortOrder, status, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM task_config WHERE task_type = #{taskType} AND status = 1 ORDER BY sort_order ASC")
    List<TaskConfig> findByTaskType(@Param("taskType") String taskType);
}
