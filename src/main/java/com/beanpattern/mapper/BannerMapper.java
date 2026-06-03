package com.beanpattern.mapper;

import com.beanpattern.entity.BannerEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BannerMapper {

    /** 获取当前有效的Banner：status=1 且在定时上下线时间范围内 */
    @Select("SELECT b.id, b.title, b.sub_title AS subTitle, b.image_url AS imageUrl, " +
            "b.link_type AS linkType, b.link_value AS linkValue, b.action_type AS actionType, " +
            "b.action_config AS actionConfig, b.tag_text AS tagText, " +
            "b.sort_order AS sortOrder, b.status, b.created_at AS createdAt " +
            "FROM bp_banner b " +
            "WHERE b.status = 1 " +
            "AND (b.start_at IS NULL OR b.start_at <= NOW()) " +
            "AND (b.end_at IS NULL OR b.end_at >= NOW()) " +
            "AND (COALESCE(b.action_type, '') <> 'ACTIVITY' OR EXISTS ( " +
            "  SELECT 1 FROM bp_activity_config a " +
            "  WHERE (a.activity_code = b.link_value OR a.banner_id = b.id) " +
            "  AND a.status = 1 AND a.start_at <= NOW() AND a.end_at >= NOW() " +
            ")) " +
            "ORDER BY b.sort_order ASC")
    List<BannerEntity> listActive();

    @Select("SELECT id, title, sub_title AS subTitle, image_url AS imageUrl, " +
            "link_type AS linkType, link_value AS linkValue, action_type AS actionType, " +
            "action_config AS actionConfig, tag_text AS tagText, " +
            "sort_order AS sortOrder, status, start_at AS startAt, end_at AS endAt, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_banner ORDER BY sort_order ASC")
    List<BannerEntity> listAll();

    @Select("SELECT id, title, sub_title AS subTitle, image_url AS imageUrl, " +
            "link_type AS linkType, link_value AS linkValue, action_type AS actionType, " +
            "action_config AS actionConfig, tag_text AS tagText, " +
            "sort_order AS sortOrder, status, start_at AS startAt, end_at AS endAt, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_banner WHERE id = #{id}")
    BannerEntity findById(@Param("id") Long id);

    @Insert("INSERT INTO bp_banner(title, sub_title, image_url, tag_text, link_type, link_value, action_type, action_config, sort_order, status, start_at, end_at) " +
            "VALUES(#{title}, #{subTitle}, #{imageUrl}, #{tagText}, #{linkType}, #{linkValue}, #{actionType}, #{actionConfig}, #{sortOrder}, #{status}, #{startAt}, #{endAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BannerEntity banner);

    @Update("UPDATE bp_banner SET title=#{title}, sub_title=#{subTitle}, image_url=#{imageUrl}, " +
            "tag_text=#{tagText}, sort_order=#{sortOrder}, link_type=#{linkType}, link_value=#{linkValue}, " +
            "action_type=#{actionType}, action_config=#{actionConfig}, start_at=#{startAt}, end_at=#{endAt}, updated_at=NOW() WHERE id=#{id}")
    int update(@Param("id") Long id, @Param("title") String title, @Param("subTitle") String subTitle,
               @Param("imageUrl") String imageUrl, @Param("tagText") String tagText,
               @Param("sortOrder") int sortOrder, @Param("linkType") String linkType, @Param("linkValue") String linkValue,
               @Param("actionType") String actionType, @Param("actionConfig") String actionConfig,
               @Param("startAt") java.time.LocalDateTime startAt, @Param("endAt") java.time.LocalDateTime endAt);

    @Update("UPDATE bp_banner SET status = CASE WHEN status=1 THEN 0 ELSE 1 END, updated_at=NOW() WHERE id=#{id}")
    int toggleStatus(@Param("id") Long id);
}
