package com.beanpattern.mapper;

import com.beanpattern.entity.ActivityConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 活动配置 Mapper：bp_activity_config
 */
@Mapper
public interface ActivityConfigMapper {

    /**
     * 查询所有活动
     */
    @Select("SELECT id, activity_code AS activityCode, title, description, cover_image AS coverImage, " +
            "activity_type AS activityType, gift_package_code AS giftPackageCode, limit_type AS limitType, " +
            "total_quota AS totalQuota, remain_quota AS remainQuota, start_at AS startAt, end_at AS endAt, " +
            "content_html AS contentHtml, content_json AS contentJson, page_type AS pageType, " +
            "button_text AS buttonText, button_action AS buttonAction, button_url AS buttonUrl, " +
            "banner_id AS bannerId, gift_items AS giftItems, discount_config AS discountConfig, " +
            "task_config AS taskConfig, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_activity_config ORDER BY created_at DESC")
    List<ActivityConfig> findAll();

    /**
     * 根据活动编码查询
     */
    @Select("SELECT id, activity_code AS activityCode, title, description, cover_image AS coverImage, " +
            "activity_type AS activityType, gift_package_code AS giftPackageCode, limit_type AS limitType, " +
            "total_quota AS totalQuota, remain_quota AS remainQuota, start_at AS startAt, end_at AS endAt, " +
            "content_html AS contentHtml, content_json AS contentJson, page_type AS pageType, " +
            "button_text AS buttonText, button_action AS buttonAction, button_url AS buttonUrl, " +
            "banner_id AS bannerId, gift_items AS giftItems, discount_config AS discountConfig, " +
            "task_config AS taskConfig, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_activity_config WHERE activity_code = #{activityCode}")
    ActivityConfig findByCode(@Param("activityCode") String activityCode);

    /**
     * 根据ID查询
     */
    @Select("SELECT id, activity_code AS activityCode, title, description, cover_image AS coverImage, " +
            "activity_type AS activityType, gift_package_code AS giftPackageCode, limit_type AS limitType, " +
            "total_quota AS totalQuota, remain_quota AS remainQuota, start_at AS startAt, end_at AS endAt, " +
            "content_html AS contentHtml, content_json AS contentJson, page_type AS pageType, " +
            "button_text AS buttonText, button_action AS buttonAction, button_url AS buttonUrl, " +
            "banner_id AS bannerId, gift_items AS giftItems, discount_config AS discountConfig, " +
            "task_config AS taskConfig, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_activity_config WHERE id = #{id}")
    ActivityConfig findById(@Param("id") Long id);

    /**
     * 查询有效的活动（已上线且在时间范围内）
     */
    @Select("SELECT id, activity_code AS activityCode, title, description, cover_image AS coverImage, " +
            "activity_type AS activityType, gift_package_code AS giftPackageCode, limit_type AS limitType, " +
            "total_quota AS totalQuota, remain_quota AS remainQuota, start_at AS startAt, end_at AS endAt, " +
            "content_html AS contentHtml, content_json AS contentJson, page_type AS pageType, " +
            "button_text AS buttonText, button_action AS buttonAction, button_url AS buttonUrl, " +
            "banner_id AS bannerId, gift_items AS giftItems, discount_config AS discountConfig, " +
            "task_config AS taskConfig, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_activity_config " +
            "WHERE status = 1 AND start_at <= NOW() AND end_at >= NOW() " +
            "ORDER BY created_at DESC")
    List<ActivityConfig> findActiveActivities();

    /**
     * 插入活动
     */
    @Insert("INSERT INTO bp_activity_config (activity_code, title, description, cover_image, " +
            "activity_type, gift_package_code, limit_type, total_quota, remain_quota, " +
            "start_at, end_at, content_html, content_json, page_type, button_text, button_action, button_url, " +
            "banner_id, gift_items, discount_config, task_config, status, created_at, updated_at) " +
            "VALUES (#{activityCode}, #{title}, #{description}, #{coverImage}, " +
            "#{activityType}, NULLIF(TRIM(#{giftPackageCode}), ''), #{limitType}, #{totalQuota}, #{remainQuota}, " +
            "#{startAt}, #{endAt}, #{contentHtml}, NULLIF(TRIM(#{contentJson}), ''), #{pageType}, #{buttonText}, #{buttonAction}, #{buttonUrl}, " +
            "#{bannerId}, #{giftItems}, #{discountConfig}, #{taskConfig}, #{status}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ActivityConfig activity);

    /**
     * 更新活动
     */
    @Update("UPDATE bp_activity_config SET title = #{title}, description = #{description}, " +
            "cover_image = #{coverImage}, activity_type = #{activityType}, " +
            "gift_package_code = NULLIF(TRIM(#{giftPackageCode}), ''), " +
            "limit_type = #{limitType}, total_quota = #{totalQuota}, remain_quota = #{remainQuota}, " +
            "start_at = #{startAt}, end_at = #{endAt}, content_html = #{contentHtml}, content_json = NULLIF(TRIM(#{contentJson}), ''), " +
            "page_type = #{pageType}, button_text = #{buttonText}, button_action = #{buttonAction}, button_url = #{buttonUrl}, " +
            "banner_id = #{bannerId}, gift_items = #{giftItems}, discount_config = #{discountConfig}, " +
            "task_config = #{taskConfig}, updated_at = NOW() WHERE id = #{id}")
    int update(ActivityConfig activity);

    /**
     * 更新活动状态
     */
    @Update("UPDATE bp_activity_config SET status = #{status}, updated_at = NOW() WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Boolean status);

    /**
     * 删除活动
     */
    @Delete("DELETE FROM bp_activity_config WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 扣减名额（原子操作）
     */
    @Update("UPDATE bp_activity_config SET remain_quota = remain_quota - 1, updated_at = NOW() " +
            "WHERE id = #{id} AND remain_quota > 0")
    int decrementQuota(@Param("id") Long id);
}
