package com.beanpattern.mapper;

import com.beanpattern.entity.PopupConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PopupConfigMapper {

    @Select("SELECT * FROM bp_popup_config WHERE enabled = 1 AND (start_time IS NULL OR start_time <= NOW()) AND (end_time IS NULL OR end_time >= NOW()) ORDER BY priority DESC")
    List<PopupConfig> findActive();

    @Select("SELECT * FROM bp_popup_config ORDER BY priority DESC")
    List<PopupConfig> findAll();

    @Select("SELECT * FROM bp_popup_config WHERE `key` = #{key}")
    PopupConfig findByKey(@Param("key") String key);

    @Insert("INSERT INTO bp_popup_config (`key`, title, content, image_url, button_text, button_url, priority, enabled, start_time, end_time, show_interval) VALUES (#{key}, #{title}, #{content}, #{imageUrl}, #{buttonText}, #{buttonUrl}, #{priority}, #{enabled}, #{startTime}, #{endTime}, #{showInterval})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PopupConfig config);

    @Update("UPDATE bp_popup_config SET title=#{title}, content=#{content}, image_url=#{imageUrl}, button_text=#{buttonText}, button_url=#{buttonUrl}, priority=#{priority}, enabled=#{enabled}, start_time=#{startTime}, end_time=#{endTime}, show_interval=#{showInterval} WHERE id=#{id}")
    void update(PopupConfig config);

    @Delete("DELETE FROM bp_popup_config WHERE id=#{id}")
    void delete(@Param("id") Long id);
}
