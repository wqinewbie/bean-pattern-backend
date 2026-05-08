package com.beanpattern.mapper;

import com.beanpattern.entity.AiMagicStyle;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * AI魔法风格Mapper
 */
@Mapper
public interface AiMagicStyleMapper {

    /**
     * 查询启用的风格列表（小程序端使用）
     */
    @Select("SELECT * FROM ai_magic_style WHERE enabled = 1 ORDER BY sort_order ASC")
    List<AiMagicStyle> findEnabled();

    /**
     * 查询所有风格列表（管理端使用）
     */
    @Select("SELECT * FROM ai_magic_style ORDER BY sort_order ASC")
    List<AiMagicStyle> findAll();

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM ai_magic_style WHERE id = #{id}")
    AiMagicStyle findById(Long id);

    /**
     * 插入风格
     */
    @Insert("INSERT INTO ai_magic_style (name, icon, category, tag, description, prompt_template, sort_order, enabled) " +
            "VALUES (#{name}, #{icon}, #{category}, #{tag}, #{description}, #{promptTemplate}, #{sortOrder}, #{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiMagicStyle style);

    /**
     * 更新风格
     */
    @Update("UPDATE ai_magic_style SET name = #{name}, icon = #{icon}, category = #{category}, " +
            "tag = #{tag}, description = #{description}, prompt_template = #{promptTemplate}, " +
            "sort_order = #{sortOrder}, enabled = #{enabled}, updated_at = NOW() WHERE id = #{id}")
    int update(AiMagicStyle style);

    /**
     * 删除风格
     */
    @Delete("DELETE FROM ai_magic_style WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 根据名称查询（用于获取提示词模板）
     */
    @Select("SELECT * FROM ai_magic_style WHERE name = #{name} AND enabled = 1")
    AiMagicStyle findByName(String name);
}
