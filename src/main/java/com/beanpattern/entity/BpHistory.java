package com.beanpattern.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BpHistory {
    private Long id;
    private Long userId;
    private String sourceType;  // LOCAL, AI
    private String brand;       // 品牌
    private Integer colorCount; // 色数
    private String name;        // 图纸名称
    private Integer gridSize;   // 尺寸
    private String rgbData;     // 原始RGB数组
    private String gridData;    // 色号索引数组
    private String colorPalette; // 颜色表JSON
    private Long boxId;         // 关联图纸箱ID
    private String sourceUrl;   // 原图URL

    // V6.0 新增字段（历史记录兼容）
    private String pixelData;     // 像素数据JSON
    private String mappedPixelData; // 前端主格式JSON（API字段，映射到pixelData）
    private String colorMapping;  // 颜色映射JSON

    private LocalDateTime expiresAt; // 过期时间
    private LocalDateTime createdAt;
}
