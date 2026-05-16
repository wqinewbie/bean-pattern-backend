package com.beanpattern.mapper;

import com.beanpattern.entity.NotificationTemplate;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 消息模板配置表 Mapper（bp_notification_template）
 */
@Mapper
public interface NotificationTemplateMapper {

    @Select("SELECT id, code, name, type, title, content, icon, " +
            "action_type AS actionType, action_value AS actionValue, action_text AS actionText, " +
            "variables, is_active AS isActive, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_notification_template WHERE id = #{id}")
    NotificationTemplate findById(@Param("id") Long id);

    @Select("SELECT id, code, name, type, title, content, icon, " +
            "action_type AS actionType, action_value AS actionValue, action_text AS actionText, " +
            "variables, is_active AS isActive, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_notification_template WHERE code = #{code}")
    NotificationTemplate findByCode(@Param("code") String code);

    @Select("SELECT id, code, name, type, title, content, icon, " +
            "action_type AS actionType, action_value AS actionValue, action_text AS actionText, " +
            "variables, is_active AS isActive, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_notification_template WHERE is_active = 1 ORDER BY id ASC")
    List<NotificationTemplate> listActive();

    @Select("SELECT id, code, name, type, title, content, icon, " +
            "action_type AS actionType, action_value AS actionValue, action_text AS actionText, " +
            "variables, is_active AS isActive, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_notification_template ORDER BY id ASC")
    List<NotificationTemplate> listAll();

    @Insert("INSERT INTO bp_notification_template(code, name, type, title, content, icon, " +
            "action_type, action_value, action_text, variables, is_active) " +
            "VALUES(#{code}, #{name}, #{type}, #{title}, #{content}, #{icon}, " +
            "#{actionType}, #{actionValue}, #{actionText}, #{variables}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(NotificationTemplate template);

    @Update("UPDATE bp_notification_template SET name = #{name}, type = #{type}, " +
            "title = #{title}, content = #{content}, icon = #{icon}, " +
            "action_type = #{actionType}, action_value = #{actionValue}, action_text = #{actionText}, " +
            "variables = #{variables}, is_active = #{isActive}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(NotificationTemplate template);

    @Update("UPDATE bp_notification_template SET is_active = #{isActive}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    @Delete("DELETE FROM bp_notification_template WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}