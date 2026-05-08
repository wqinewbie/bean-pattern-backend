package com.beanpattern.mapper;

import com.beanpattern.entity.PrivilegeConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 权益配置表 Mapper（bp_privilege_config）
 */
@Mapper
public interface PrivilegeConfigMapper {

    @Select("SELECT id, config_key AS configKey, config_name AS configName, " +
            "free_value AS freeValue, vip_value AS vipValue, value_type AS valueType, " +
            "description, sort_order AS sortOrder, is_active AS isActive, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_privilege_config WHERE id = #{id}")
    PrivilegeConfig findById(@Param("id") Long id);

    @Select("SELECT id, config_key AS configKey, config_name AS configName, " +
            "free_value AS freeValue, vip_value AS vipValue, value_type AS valueType, " +
            "description, sort_order AS sortOrder, is_active AS isActive, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_privilege_config WHERE config_key = #{configKey}")
    PrivilegeConfig findByKey(@Param("configKey") String configKey);

    @Select("SELECT id, config_key AS configKey, config_name AS configName, " +
            "free_value AS freeValue, vip_value AS vipValue, value_type AS valueType, " +
            "description, sort_order AS sortOrder, is_active AS isActive, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_privilege_config WHERE is_active = 1 ORDER BY sort_order ASC")
    List<PrivilegeConfig> listActive();

    @Select("SELECT id, config_key AS configKey, config_name AS configName, " +
            "free_value AS freeValue, vip_value AS vipValue, value_type AS valueType, " +
            "description, sort_order AS sortOrder, is_active AS isActive, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_privilege_config ORDER BY sort_order ASC")
    List<PrivilegeConfig> listAll();

    @Update("UPDATE bp_privilege_config SET free_value = #{freeValue}, " +
            "vip_value = #{vipValue}, description = #{description}, " +
            "sort_order = #{sortOrder}, is_active = #{isActive}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(PrivilegeConfig privilegeConfig);
}
