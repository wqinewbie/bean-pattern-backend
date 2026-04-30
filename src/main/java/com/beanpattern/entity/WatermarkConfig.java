package com.beanpattern.entity;

/**
 * 水印配置实体
 */
public class WatermarkConfig {
    
    private Long id;
    private String appName;          // 小程序名称
    private String defaultText;      // 默认水印文字
    private Integer fontSize;        // 字体大小
    private String color;            // 颜色
    private Integer angle;           // 倾斜角度
    private Double spacingXRatio;    // 水平间距比例
    private Double spacingYRatio;    // 垂直间距比例
    private Double opacity;          // 透明度
    private String createdAt;
    private String updatedAt;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    
    public String getDefaultText() { return defaultText; }
    public void setDefaultText(String defaultText) { this.defaultText = defaultText; }
    
    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public Integer getAngle() { return angle; }
    public void setAngle(Integer angle) { this.angle = angle; }
    
    public Double getSpacingXRatio() { return spacingXRatio; }
    public void setSpacingXRatio(Double spacingXRatio) { this.spacingXRatio = spacingXRatio; }
    
    public Double getSpacingYRatio() { return spacingYRatio; }
    public void setSpacingYRatio(Double spacingYRatio) { this.spacingYRatio = spacingYRatio; }
    
    public Double getOpacity() { return opacity; }
    public void setOpacity(Double opacity) { this.opacity = opacity; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
