package com.beanpattern.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BpDraft {
    private Long id;
    private Long userId;
    private String sourceType;   // DRAW, EDIT
    private String brand;        // 品牌（画板模式下可能为空）
    private Integer colorCount;  // 色数（可能为空）
    private String name;         // 图纸名称
    private Integer gridSize;    // 尺寸
    private Long boxId;          // 关联图纸箱ID

    private String mappedPixelData; // 主图案JSON(二维像素对象)

    private LocalDateTime expiresAt; // 过期时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
