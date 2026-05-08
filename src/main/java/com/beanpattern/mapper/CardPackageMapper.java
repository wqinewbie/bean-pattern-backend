package com.beanpattern.mapper;

import com.beanpattern.entity.CardPackage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 次卡套餐配置表 Mapper（bp_card_package）
 */
@Mapper
public interface CardPackageMapper {

    @Select("SELECT id, package_code AS packageCode, package_name AS packageName, " +
            "ai_quota AS aiQuota, price, original_price AS originalPrice, " +
            "vip_price AS vipPrice, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_card_package WHERE id = #{id}")
    CardPackage findById(@Param("id") Long id);

    @Select("SELECT id, package_code AS packageCode, package_name AS packageName, " +
            "ai_quota AS aiQuota, price, original_price AS originalPrice, " +
            "vip_price AS vipPrice, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_card_package WHERE package_code = #{packageCode}")
    CardPackage findByCode(@Param("packageCode") String packageCode);

    @Select("SELECT id, package_code AS packageCode, package_name AS packageName, " +
            "ai_quota AS aiQuota, price, original_price AS originalPrice, " +
            "vip_price AS vipPrice, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_card_package WHERE is_active = 1 ORDER BY sort_order ASC")
    List<CardPackage> listActive();

    @Select("SELECT id, package_code AS packageCode, package_name AS packageName, " +
            "ai_quota AS aiQuota, price, original_price AS originalPrice, " +
            "vip_price AS vipPrice, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_card_package ORDER BY sort_order ASC")
    List<CardPackage> listAll();

    @Insert("INSERT INTO bp_card_package(package_code, package_name, ai_quota, price, " +
            "original_price, vip_price, tag, sort_order, is_active) " +
            "VALUES(#{packageCode}, #{packageName}, #{aiQuota}, #{price}, " +
            "#{originalPrice}, #{vipPrice}, #{tag}, #{sortOrder}, #{isActive})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CardPackage cardPackage);

    @Update("UPDATE bp_card_package SET package_name = #{packageName}, " +
            "ai_quota = #{aiQuota}, price = #{price}, " +
            "original_price = #{originalPrice}, vip_price = #{vipPrice}, " +
            "tag = #{tag}, sort_order = #{sortOrder}, is_active = #{isActive}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(CardPackage cardPackage);

    @Update("UPDATE bp_card_package SET is_active = #{isActive}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    @Delete("DELETE FROM bp_card_package WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
