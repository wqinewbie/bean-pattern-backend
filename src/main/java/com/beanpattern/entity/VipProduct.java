package com.beanpattern.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VIP商品表实体：vip_product
 */
public class VipProduct {
    private Long id;
    private String productCode;
    private String name;
    private String description;
    private Integer vipLevel;
    private Integer aiQuotaPerMonth;
    private Integer storageQuota;
    private Integer draftQuota;
    private String availableBrands;
    private Integer colorLimitPerBrand;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String wxProductId;
    private Integer validDays;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getVipLevel() { return vipLevel; }
    public void setVipLevel(Integer vipLevel) { this.vipLevel = vipLevel; }

    public Integer getAiQuotaPerMonth() { return aiQuotaPerMonth; }
    public void setAiQuotaPerMonth(Integer aiQuotaPerMonth) { this.aiQuotaPerMonth = aiQuotaPerMonth; }

    public Integer getStorageQuota() { return storageQuota; }
    public void setStorageQuota(Integer storageQuota) { this.storageQuota = storageQuota; }

    public Integer getDraftQuota() { return draftQuota; }
    public void setDraftQuota(Integer draftQuota) { this.draftQuota = draftQuota; }

    public String getAvailableBrands() { return availableBrands; }
    public void setAvailableBrands(String availableBrands) { this.availableBrands = availableBrands; }

    public Integer getColorLimitPerBrand() { return colorLimitPerBrand; }
    public void setColorLimitPerBrand(Integer colorLimitPerBrand) { this.colorLimitPerBrand = colorLimitPerBrand; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public String getWxProductId() { return wxProductId; }
    public void setWxProductId(String wxProductId) { this.wxProductId = wxProductId; }

    public Integer getValidDays() { return validDays; }
    public void setValidDays(Integer validDays) { this.validDays = validDays; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
