package com.beanpattern.mapper;

import com.beanpattern.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysDictItemMapper {

    @Select("SELECT id, dict_type AS dictType, dict_label AS dictLabel, dict_value AS dictValue, "
            + "tag_type AS tagType, sort_order AS sortOrder, status, disabled, remark, "
            + "created_at AS createdAt, updated_at AS updatedAt "
            + "FROM bp_sys_dict_item WHERE status = 1 ORDER BY dict_type ASC, sort_order ASC, id ASC")
    List<SysDictItem> findAllEnabled();
}
