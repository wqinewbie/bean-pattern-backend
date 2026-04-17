package com.beanpattern.mapper;

import com.beanpattern.entity.TutorialEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TutorialMapper {

    @Select("SELECT id, title, description, video_url AS videoUrl, thumbnail_url AS thumbnailUrl, " +
            "sort_order AS sortOrder, status, created_at AS createdAt " +
            "FROM bp_tutorial ORDER BY sort_order ASC")
    List<TutorialEntity> listAll();

    @Select("SELECT id, title, description, video_url AS videoUrl, thumbnail_url AS thumbnailUrl, " +
            "sort_order AS sortOrder, status, created_at AS createdAt " +
            "FROM bp_tutorial WHERE status = 1 ORDER BY sort_order ASC")
    List<TutorialEntity> listActive();

    @Insert("INSERT INTO bp_tutorial(title, description, video_url, thumbnail_url, sort_order, status) " +
            "VALUES(#{title}, #{description}, #{videoUrl}, #{thumbnailUrl}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TutorialEntity tutorial);

    @Update("UPDATE bp_tutorial SET title=#{title}, description=#{description}, " +
            "video_url=#{videoUrl}, thumbnail_url=#{thumbnailUrl}, " +
            "sort_order=#{sortOrder}, updated_at=NOW() WHERE id=#{id}")
    int update(TutorialEntity tutorial);

    @Update("UPDATE bp_tutorial SET status = CASE WHEN status=1 THEN 0 ELSE 1 END, updated_at=NOW() WHERE id=#{id}")
    int toggleStatus(@Param("id") Long id);
}
