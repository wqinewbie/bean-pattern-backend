package com.beanpattern.mapper;

import com.beanpattern.entity.TaskConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 任务配置 Mapper：task_config
 */
@Mapper
public interface TaskConfigMapper {

    @Select("SELECT id, task_code AS taskCode, task_name AS taskName, task_type AS taskType, " +
            "description, reward_type AS rewardType, reward_value AS rewardValue, " +
            "icon, sort_order AS sortOrder, is_active AS isActive, extra_config AS extraConfig, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_task_config WHERE is_active = 1 ORDER BY sort_order ASC")
    List<TaskConfig> findAllActive();

    @Select("SELECT id, task_code AS taskCode, task_name AS taskName, task_type AS taskType, " +
            "description, reward_type AS rewardType, reward_value AS rewardValue, " +
            "icon, sort_order AS sortOrder, is_active AS isActive, extra_config AS extraConfig, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_task_config ORDER BY sort_order ASC")
    List<TaskConfig> findAll();

    @Select("SELECT id, task_code AS taskCode, task_name AS taskName, task_type AS taskType, " +
            "description, reward_type AS rewardType, reward_value AS rewardValue, " +
            "icon, sort_order AS sortOrder, is_active AS isActive, extra_config AS extraConfig, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_task_config WHERE task_code = #{taskCode}")
    TaskConfig findByCode(@Param("taskCode") String taskCode);

    @Select("SELECT id, task_code AS taskCode, task_name AS taskName, task_type AS taskType, " +
            "description, reward_type AS rewardType, reward_value AS rewardValue, " +
            "icon, sort_order AS sortOrder, is_active AS isActive, extra_config AS extraConfig, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_task_config WHERE id = #{id}")
    TaskConfig findById(@Param("id") Long id);

    @Select("SELECT id, task_code AS taskCode, task_name AS taskName, task_type AS taskType, " +
            "description, reward_type AS rewardType, reward_value AS rewardValue, " +
            "icon, sort_order AS sortOrder, is_active AS isActive, extra_config AS extraConfig, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_task_config WHERE task_type = #{taskType} AND is_active = 1 ORDER BY sort_order ASC")
    List<TaskConfig> findByTaskType(@Param("taskType") String taskType);

    @Insert("INSERT INTO bp_task_config (task_code, task_name, task_type, description, reward_type, reward_value, " +
            "icon, sort_order, is_active, extra_config, created_at, updated_at) " +
            "VALUES (#{taskCode}, #{taskName}, #{taskType}, #{description}, #{rewardType}, #{rewardValue}, " +
            "#{icon}, #{sortOrder}, #{isActive}, #{extraConfig}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(TaskConfig taskConfig);

    @Update("UPDATE bp_task_config SET task_name = #{taskName}, task_type = #{taskType}, " +
            "description = #{description}, reward_type = #{rewardType}, reward_value = #{rewardValue}, " +
            "icon = #{icon}, sort_order = #{sortOrder}, extra_config = #{extraConfig}, updated_at = NOW() " +
            "WHERE id = #{id}")
    void update(TaskConfig taskConfig);

    @Update("UPDATE bp_task_config SET is_active = #{isActive}, updated_at = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    @Delete("DELETE FROM bp_task_config WHERE id = #{id}")
    void deleteById(@Param("id") Long id);
}
