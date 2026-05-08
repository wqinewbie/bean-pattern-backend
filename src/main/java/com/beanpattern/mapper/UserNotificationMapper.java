package com.beanpattern.mapper;

import com.beanpattern.entity.UserNotification;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户通知 Mapper：bp_user_notification
 */
@Mapper
public interface UserNotificationMapper {

    @Select("SELECT id, user_id AS userId, type, template_code AS templateCode, title, content, icon, " +
            "action_type AS actionType, action_value AS actionValue, action_text AS actionText, " +
            "is_read AS isRead, related_type AS relatedType, related_id AS relatedId, " +
            "extra_data AS extraData, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_notification WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT #{limit}")
    List<UserNotification> findByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    @Select("SELECT COUNT(*) FROM bp_user_notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnreadByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id AS userId, type, template_code AS templateCode, title, content, icon, " +
            "action_type AS actionType, action_value AS actionValue, action_text AS actionText, " +
            "is_read AS isRead, related_type AS relatedType, related_id AS relatedId, " +
            "extra_data AS extraData, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_user_notification WHERE id = #{id}")
    UserNotification findById(@Param("id") Long id);

    @Insert("INSERT INTO bp_user_notification(user_id, type, template_code, title, content, icon, " +
            "action_type, action_value, action_text, is_read, related_type, related_id, extra_data) " +
            "VALUES(#{userId}, #{type}, #{templateCode}, #{title}, #{content}, #{icon}, " +
            "#{actionType}, #{actionValue}, #{actionText}, 0, #{relatedType}, #{relatedId}, #{extraData})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserNotification notification);

    @Update("UPDATE bp_user_notification SET is_read = 1, updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int markAsRead(@Param("id") Long id);

    @Update("UPDATE bp_user_notification SET is_read = 1, updated_at = CURRENT_TIMESTAMP WHERE user_id = #{userId}")
    int markAllAsRead(@Param("userId") Long userId);

    @Delete("DELETE FROM bp_user_notification WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM bp_user_notification WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
