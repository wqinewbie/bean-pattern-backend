package com.beanpattern.mapper;

import com.beanpattern.entity.AiSizePreset;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AiSizePresetMapper {

    String BASE_COLUMNS = "id, preset_key AS presetKey, name, description, grid_min AS gridMin, " +
            "grid_max AS gridMax, candidate_grids AS candidateGrids, default_grid AS defaultGrid, " +
            "sort_order AS sortOrder, recommended, enabled, remark, created_at AS createdAt, updated_at AS updatedAt";

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_ai_size_preset WHERE enabled = 1 ORDER BY sort_order ASC, id ASC")
    List<AiSizePreset> findEnabled();

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_ai_size_preset ORDER BY sort_order ASC, id ASC")
    List<AiSizePreset> findAll();

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_ai_size_preset WHERE id = #{id}")
    AiSizePreset findById(@Param("id") Long id);

    @Select("SELECT " + BASE_COLUMNS + " FROM bp_ai_size_preset WHERE preset_key = #{presetKey} AND enabled = 1")
    AiSizePreset findEnabledByKey(@Param("presetKey") String presetKey);

    @Select("SELECT COUNT(*) FROM bp_ai_size_preset WHERE preset_key = #{presetKey} AND (#{excludeId} IS NULL OR id <> #{excludeId})")
    int countByKey(@Param("presetKey") String presetKey, @Param("excludeId") Long excludeId);

    @Select("SELECT COUNT(*) FROM bp_ai_size_preset WHERE enabled = 1 AND (#{excludeId} IS NULL OR id <> #{excludeId})")
    int countEnabledExcept(@Param("excludeId") Long excludeId);

    @Insert("INSERT INTO bp_ai_size_preset (preset_key, name, description, grid_min, grid_max, candidate_grids, default_grid, " +
            "sort_order, recommended, enabled, remark) VALUES (#{presetKey}, #{name}, #{description}, #{gridMin}, #{gridMax}, " +
            "#{candidateGrids}, #{defaultGrid}, #{sortOrder}, #{recommended}, #{enabled}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiSizePreset preset);

    @Update("UPDATE bp_ai_size_preset SET name = #{name}, description = #{description}, grid_min = #{gridMin}, " +
            "grid_max = #{gridMax}, candidate_grids = #{candidateGrids}, default_grid = #{defaultGrid}, " +
            "sort_order = #{sortOrder}, recommended = #{recommended}, enabled = #{enabled}, remark = #{remark}, " +
            "updated_at = NOW() WHERE id = #{id}")
    int update(AiSizePreset preset);

    @Delete("DELETE FROM bp_ai_size_preset WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
