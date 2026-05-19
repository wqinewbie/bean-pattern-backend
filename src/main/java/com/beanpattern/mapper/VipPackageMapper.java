package com.beanpattern.mapper;

import com.beanpattern.entity.VipPackage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 会员套餐配置表 Mapper（bp_vip_package）
 */
@Mapper
public interface VipPackageMapper {

    @Select("SELECT id, package_code AS packageCode, midas_product_id AS midasProductId, package_name AS packageName, " +
            "duration_days AS durationDays, price, original_price AS originalPrice, " +
            "ai_quota_gift AS aiQuotaGift, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, purchase_limit AS purchaseLimit, " +
            "shelf_start_time AS shelfStartTime, shelf_end_time AS shelfEndTime, " +
            "vip_only AS vipOnly, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_vip_package WHERE id = #{id}")
    VipPackage findById(@Param("id") Long id);

    @Select("SELECT id, package_code AS packageCode, midas_product_id AS midasProductId, package_name AS packageName, " +
            "duration_days AS durationDays, price, original_price AS originalPrice, " +
            "ai_quota_gift AS aiQuotaGift, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, purchase_limit AS purchaseLimit, " +
            "shelf_start_time AS shelfStartTime, shelf_end_time AS shelfEndTime, " +
            "vip_only AS vipOnly, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_vip_package WHERE package_code = #{packageCode}")
    VipPackage findByCode(@Param("packageCode") String packageCode);

    @Select("SELECT id, package_code AS packageCode, midas_product_id AS midasProductId, package_name AS packageName, " +
            "duration_days AS durationDays, price, original_price AS originalPrice, " +
            "ai_quota_gift AS aiQuotaGift, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, purchase_limit AS purchaseLimit, " +
            "shelf_start_time AS shelfStartTime, shelf_end_time AS shelfEndTime, " +
            "vip_only AS vipOnly, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_vip_package WHERE is_active = 1 " +
            "AND (shelf_start_time IS NULL OR shelf_start_time <= NOW()) " +
            "AND (shelf_end_time IS NULL OR shelf_end_time >= NOW()) " +
            "ORDER BY sort_order ASC")
    List<VipPackage> listActive();

    @Select("SELECT id, package_code AS packageCode, midas_product_id AS midasProductId, package_name AS packageName, " +
            "duration_days AS durationDays, price, original_price AS originalPrice, " +
            "ai_quota_gift AS aiQuotaGift, tag, sort_order AS sortOrder, " +
            "is_active AS isActive, purchase_limit AS purchaseLimit, " +
            "shelf_start_time AS shelfStartTime, shelf_end_time AS shelfEndTime, " +
            "vip_only AS vipOnly, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM bp_vip_package ORDER BY sort_order ASC")
    List<VipPackage> listAll();

    @Insert("INSERT INTO bp_vip_package(package_code, midas_product_id, package_name, duration_days, price, " +
            "original_price, ai_quota_gift, tag, sort_order, is_active, purchase_limit, " +
            "shelf_start_time, shelf_end_time, vip_only) " +
            "VALUES(#{packageCode}, #{midasProductId}, #{packageName}, #{durationDays}, #{price}, " +
            "#{originalPrice}, #{aiQuotaGift}, #{tag}, #{sortOrder}, #{isActive}, " +
            "#{purchaseLimit}, #{shelfStartTime}, #{shelfEndTime}, #{vipOnly})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(VipPackage vipPackage);

    @Update("UPDATE bp_vip_package SET midas_product_id = #{midasProductId}, package_name = #{packageName}, " +
            "duration_days = #{durationDays}, price = #{price}, " +
            "original_price = #{originalPrice}, ai_quota_gift = #{aiQuotaGift}, " +
            "tag = #{tag}, sort_order = #{sortOrder}, is_active = #{isActive}, " +
            "purchase_limit = #{purchaseLimit}, shelf_start_time = #{shelfStartTime}, " +
            "shelf_end_time = #{shelfEndTime}, vip_only = #{vipOnly}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int update(VipPackage vipPackage);

    @Update("UPDATE bp_vip_package SET is_active = #{isActive}, " +
            "updated_at = CURRENT_TIMESTAMP WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    @Delete("DELETE FROM bp_vip_package WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}
