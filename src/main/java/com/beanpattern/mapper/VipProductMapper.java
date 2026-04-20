package com.beanpattern.mapper;

import com.beanpattern.entity.VipProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * VIP商品 Mapper：vip_product
 */
@Mapper
public interface VipProductMapper {

    @Select("SELECT id, product_code AS productCode, name, description, vip_level AS vipLevel, " +
            "ai_quota_per_month AS aiQuotaPerMonth, storage_quota AS storageQuota, draft_quota AS draftQuota, " +
            "available_brands AS availableBrands, color_limit_per_brand AS colorLimitPerBrand, " +
            "price, original_price AS originalPrice, wx_product_id AS wxProductId, valid_days AS validDays, " +
            "sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM vip_product WHERE status = 1 ORDER BY sort_order ASC")
    List<VipProduct> findAllActive();

    @Select("SELECT id, product_code AS productCode, name, description, vip_level AS vipLevel, " +
            "ai_quota_per_month AS aiQuotaPerMonth, storage_quota AS storageQuota, draft_quota AS draftQuota, " +
            "available_brands AS availableBrands, color_limit_per_brand AS colorLimitPerBrand, " +
            "price, original_price AS originalPrice, wx_product_id AS wxProductId, valid_days AS validDays, " +
            "sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM vip_product WHERE product_code = #{productCode}")
    VipProduct findByCode(@Param("productCode") String productCode);

    @Select("SELECT id, product_code AS productCode, name, description, vip_level AS vipLevel, " +
            "ai_quota_per_month AS aiQuotaPerMonth, storage_quota AS storageQuota, draft_quota AS draftQuota, " +
            "available_brands AS availableBrands, color_limit_per_brand AS colorLimitPerBrand, " +
            "price, original_price AS originalPrice, wx_product_id AS wxProductId, valid_days AS validDays, " +
            "sort_order AS sortOrder, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM vip_product WHERE id = #{id}")
    VipProduct findById(@Param("id") Long id);
}
