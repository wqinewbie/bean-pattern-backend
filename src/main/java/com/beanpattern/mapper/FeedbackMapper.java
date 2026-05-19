package com.beanpattern.mapper;

import com.beanpattern.entity.FeedbackEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FeedbackMapper {

    @Insert("INSERT INTO bp_feedback(user_id, content, images, contact, category, status) " +
            "VALUES(#{userId}, #{content}, #{images}, #{contact}, #{category}, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FeedbackEntity feedback);

    @Select("SELECT id, user_id AS userId, content, category, status, created_at AS createdAt " +
            "FROM bp_feedback ORDER BY created_at DESC LIMIT 200")
    List<FeedbackEntity> listAll();

    @Update("UPDATE bp_feedback SET status=#{status} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") int status);

    @Delete("DELETE FROM bp_feedback WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);
}
