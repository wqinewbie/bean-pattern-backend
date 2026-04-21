package com.beanpattern.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BpHistory {
    private Long id;
    private Long userId;
    private String sourceType;   // LOCAL, AI
    private String brand;        // 品牌
    private Integer colorCount;  // 色数
    private String name;         // 图纸名称
    private Integer gridSize;    // 尺寸
    private Long boxId;          // 关联图纸箱ID
    private String sourceUrl;    // 原图URL

    private String mappedPixelData; // 主图案JSON(二维像素对象)

    private LocalDateTime expiresAt; // 过期时间
    private LocalDateTime createdAt;
}
