package com.beanpattern.entity;

/**
 * 用户水印配置实体（VIP功能）
 */
public class UserWatermarkConfig {
    
    private Long id;
    private Long userId;
    private Integer enabled;      // 0=关闭 1=开启
    private String customText;    // 自定义水印文字
    private String createdAt;
    private String updatedAt;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
    
    public String getCustomText() { return customText; }
    public void setCustomText(String customText) { this.customText = customText; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
}
