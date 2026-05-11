package com.beanpattern.mapper;

import com.beanpattern.entity.CheckinConfig;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 签到配置 Mapper：bp_checkin_config
 */
@Mapper
public interface CheckinConfigMapper {

    @Select("SELECT id, continuous_days_required AS continuousDaysRequired, reward_type AS rewardType, " +
            "reward_value AS rewardValue, is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_checkin_config ORDER BY id DESC LIMIT 1")
    CheckinConfig findLatest();

    @Insert("INSERT INTO bp_checkin_config(continuous_days_required, reward_type, reward_value, is_active, created_at, updated_at) " +
            "VALUES(#{continuousDaysRequired}, #{rewardType}, #{rewardValue}, #{isActive}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CheckinConfig config);

    @Update("UPDATE bp_checkin_config SET continuous_days_required = #{continuousDaysRequired}, reward_type = #{rewardType}, " +
            "reward_value = #{rewardValue}, is_active = #{isActive}, updated_at = NOW() WHERE id = #{id}")
    int update(CheckinConfig config);
}
