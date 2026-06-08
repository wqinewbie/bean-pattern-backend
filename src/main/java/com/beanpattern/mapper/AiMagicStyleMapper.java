package com.beanpattern.mapper;

import com.beanpattern.entity.AiMagicStyle;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface AiMagicStyleMapper {

    String BASE_COLUMNS = "id, name, icon, tag, description, prompt_template AS promptTemplate, " +
            "negative_prompt_template AS negativePromptTemplate, model_key AS modelKey, " +
            "sort_order AS sortOrder, enabled, created_at AS createdAt, updated_at AS updatedAt";

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_ai_magic_style WHERE enabled = 1 ORDER BY sort_order ASC")
    List<AiMagicStyle> findEnabled();

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_ai_magic_style ORDER BY sort_order ASC")
    List<AiMagicStyle> findAll();

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_ai_magic_style WHERE id = #{id}")
    AiMagicStyle findById(Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_ai_magic_style WHERE name = #{name} AND enabled = 1")
    AiMagicStyle findByName(String name);

    @Insert("INSERT INTO bp_ai_magic_style (name, icon, tag, description, prompt_template, negative_prompt_template, model_key, sort_order, enabled) " +
            "VALUES (#{name}, #{icon}, #{tag}, #{description}, #{promptTemplate}, #{negativePromptTemplate}, #{modelKey}, #{sortOrder}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiMagicStyle style);

    @Update("UPDATE bp_ai_magic_style SET name = #{name}, icon = #{icon}, " +
            "tag = #{tag}, description = #{description}, prompt_template = #{promptTemplate}, " +
            "negative_prompt_template = #{negativePromptTemplate}, " +
            "model_key = #{modelKey}, " +
            "sort_order = #{sortOrder}, enabled = #{enabled}, updated_at = NOW() WHERE id = #{id}")
    int update(AiMagicStyle style);

    @Delete("DELETE FROM bp_ai_magic_style WHERE id = #{id}")
    int deleteById(Long id);
}
