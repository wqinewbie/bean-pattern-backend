package com.beanpattern.mapper;

import com.beanpattern.entity.SysDictItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SysDictItemMapper {

    @Select("SELECT id, dict_type AS dictType, dict_label AS dictLabel, dict_value AS dictValue, "
            + "tag_type AS tagType, sort_order AS sortOrder, status, disabled, remark, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM bp_sys_dict_item WHERE status = 1 ORDER BY dict_type ASC, sort_order ASC, id ASC")
    List<SysDictItem> findAllEnabled();

    @Select("SELECT id, dict_type AS dictType, dict_label AS dictLabel, dict_value AS dictValue, "
            + "tag_type AS tagType, sort_order AS sortOrder, status, disabled, remark, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM bp_sys_dict_item ORDER BY dict_type ASC, sort_order ASC, id ASC")
    List<SysDictItem> findAll();

    @Select("SELECT id, dict_type AS dictType, dict_label AS dictLabel, dict_value AS dictValue, "
            + "tag_type AS tagType, sort_order AS sortOrder, status, disabled, remark, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM bp_sys_dict_item WHERE id = #{id}")
    SysDictItem findById(Long id);

    @Insert("INSERT INTO bp_sys_dict_item (dict_type, dict_label, dict_value, tag_type, sort_order, status, disabled, remark, created_at, updated_at) "
            + "VALUES (#{dictType}, #{dictLabel}, #{dictValue}, #{tagType}, #{sortOrder}, #{status}, #{disabled}, #{remark}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysDictItem item);

    @Update("UPDATE bp_sys_dict_item SET dict_type = #{dictType}, dict_label = #{dictLabel}, dict_value = #{dictValue}, "
            + "tag_type = #{tagType}, sort_order = #{sortOrder}, status = #{status}, disabled = #{disabled}, remark = #{remark}, updated_at = NOW() "
            + "WHERE id = #{id}")
    int update(SysDictItem item);

    @Delete("DELETE FROM bp_sys_dict_item WHERE id = #{id}")
    int deleteById(Long id);
}
