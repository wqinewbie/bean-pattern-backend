package com.beanpattern.entity;

/**
 * 水印配置实体
 */
public class WatermarkConfig {
    
    private Long id;
    private Integer enabled;     // 0=禁用 1=启用
    private String text;         // 水印文字
    private Integer fontSize;    // 字体大小
    private String color;        // 颜色
    private String position;     // 位置
    private Double opacity;       // 透明度
    private Integer margin;       // 边距
    private String createdAt;
    private String updatedAt;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public Integer getFontSize() { return fontSize; }
    public void setFontSize(Integer fontSize) { this.fontSize = fontSize; }
    
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public Double getOpacity() { return opacity; }
    public void setOpacity(Double opacity) { this.opacity = opacity; }
    
    public Integer getMargin() { return margin; }
    public void setMargin(Integer margin) { this.margin = margin; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    
    public boolean isEnabled() {
        return enabled != null && enabled == 1;
    }
}
