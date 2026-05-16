package com.beanpattern.mapper;

import com.beanpattern.entity.BannerEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BannerMapper {

    /** 获取当前有效的Banner：status=1 且在定时上下线时间范围内 */
    @Select("SELECT id, title, sub_title AS subTitle, image_url AS imageUrl, " +
            "link_type AS linkType, link_value AS linkValue, action_type AS actionType, " +
            "action_config AS actionConfig, tag_text AS tagText, bg_color AS bgColor, " +
            "sort_order AS sortOrder, status, created_at AS createdAt " +
            "FROM bp_banner WHERE status = 1 " +
            "AND (start_at IS NULL OR start_at <= NOW()) " +
            "AND (end_at IS NULL OR end_at >= NOW()) " +
            "ORDER BY sort_order ASC LIMIT 10")
    List<BannerEntity> listActive();

    @Select("SELECT id, title, sub_title AS subTitle, image_url AS imageUrl, " +
            "link_type AS linkType, link_value AS linkValue, action_type AS actionType, " +
            "action_config AS actionConfig, tag_text AS tagText, bg_color AS bgColor, " +
            "sort_order AS sortOrder, status, created_at AS createdAt " +
            "FROM bp_banner ORDER BY sort_order ASC")
    List<BannerEntity> listAll();

    @Select("SELECT id, title, sub_title AS subTitle, image_url AS imageUrl, " +
            "link_type AS linkType, link_value AS linkValue, action_type AS actionType, " +
            "action_config AS actionConfig, tag_text AS tagText, bg_color AS bgColor, " +
            "sort_order AS sortOrder, status, start_at AS startAt, end_at AS endAt, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_banner WHERE id = #{id}")
    BannerEntity findById(@Param("id") Long id);

    @Insert("INSERT INTO bp_banner(title, sub_title, image_url, tag_text, bg_color, link_type, link_value, action_type, action_config, sort_order, status) " +
            "VALUES(#{title}, #{subTitle}, #{imageUrl}, #{tagText}, #{bgColor}, #{linkType}, #{linkValue}, #{actionType}, #{actionConfig}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BannerEntity banner);

    @Update("UPDATE bp_banner SET title=#{title}, sub_title=#{subTitle}, image_url=#{imageUrl}, " +
            "tag_text=#{tagText}, bg_color=#{bgColor}, sort_order=#{sortOrder}, link_type=#{linkType}, link_value=#{linkValue}, " +
            "action_type=#{actionType}, action_config=#{actionConfig}, start_at=#{startAt}, end_at=#{endAt}, updated_at=NOW() WHERE id=#{id}")
    int update(@Param("id") Long id, @Param("title") String title, @Param("subTitle") String subTitle,
               @Param("imageUrl") String imageUrl, @Param("tagText") String tagText, @Param("bgColor") String bgColor,
               @Param("sortOrder") int sortOrder, @Param("linkType") String linkType, @Param("linkValue") String linkValue,
               @Param("actionType") String actionType, @Param("actionConfig") String actionConfig,
               @Param("startAt") java.time.LocalDateTime startAt, @Param("endAt") java.time.LocalDateTime endAt);

    @Update("UPDATE bp_banner SET status = CASE WHEN status=1 THEN 0 ELSE 1 END, updated_at=NOW() WHERE id=#{id}")
    int toggleStatus(@Param("id") Long id);
}
