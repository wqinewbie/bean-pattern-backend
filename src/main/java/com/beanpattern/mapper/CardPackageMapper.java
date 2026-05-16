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
            "is_active AS isActive, purchase_limit AS purchaseLimit, " +
            "shelf_start_time AS shelfStartTime, shelf_end_time AS shelfEndTime, " +
            "vip_only AS vipOnly, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_card_package WHERE id = #{id}")
    CardPackage findById(@Param("id") Long id);

    @Select("SELECT id, package_code AS packageCode, package_name AS packageName, " +
            "ai_quota AS aiQuota, price, original_price AS originalPrice, " +
            "vip_price AS vipPrice, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, purchase_limit AS purchaseLimit, " +
            "shelf_start_time AS shelfStartTime, shelf_end_time AS shelfEndTime, " +
            "vip_only AS vipOnly, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_card_package WHERE package_code = #{packageCode}")
    CardPackage findByCode(@Param("packageCode") String packageCode);

    @Select("SELECT id, package_code AS packageCode, package_name AS packageName, " +
            "ai_quota AS aiQuota, price, original_price AS originalPrice, " +
            "vip_price AS vipPrice, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, purchase_limit AS purchaseLimit, " +
            "shelf_start_time AS shelfStartTime, shelf_end_time AS shelfEndTime, " +
            "vip_only AS vipOnly, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_card_package WHERE is_active = 1 " +
            "AND (shelf_start_time IS NULL OR shelf_start_time <= NOW()) " +
            "AND (shelf_end_time IS NULL OR shelf_end_time >= NOW()) " +
            "ORDER BY sort_order ASC")
    List<CardPackage> listActive();

    @Select("SELECT id, package_code AS packageCode, package_name AS packageName, " +
            "ai_quota AS aiQuota, price, original_price AS originalPrice, " +
            "vip_price AS vipPrice, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, purchase_limit AS purchaseLimit, " +
            "shelf_start_time AS shelfStartTime, shelf_end_time AS shelfEndTime, " +
            "vip_only AS vipOnly, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_card_package ORDER BY sort_order ASC")
    List<CardPackage> listAll();

    @Insert("INSERT INTO bp_card_package(package_code, package_name, ai_quota, price, " +
            "original_price, vip_price, tag, sort_order, is_active, purchase_limit, " +
            "shelf_start_time, shelf_end_time, vip_only) " +
            "VALUES(#{packageCode}, #{packageName}, #{aiQuota}, #{price}, " +
            "#{originalPrice}, #{vipPrice}, #{tag}, #{sortOrder}, #{isActive}, " +
            "#{purchaseLimit}, #{shelfStartTime}, #{shelfEndTime}, #{vipOnly})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CardPackage cardPackage);

    @Update("UPDATE bp_card_package SET package_name = #{packageName}, " +
            "ai_quota = #{aiQuota}, price = #{price}, " +
            "original_price = #{originalPrice}, vip_price = #{vipPrice}, " +
            "tag = #{tag}, sort_order = #{sortOrder}, is_active = #{isActive}, " +
            "purchase_limit = #{purchaseLimit}, shelf_start_time = #{shelfStartTime}, " +
            "shelf_end_time = #{shelfEndTime}, vip_only = #{vipOnly}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(CardPackage cardPackage);

    @Update("UPDATE bp_card_package SET is_active = #{isActive}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    @Delete("DELETE FROM bp_card_package WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
