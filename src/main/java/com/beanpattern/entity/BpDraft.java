package com.beanpattern.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BpDraft {
    private Long id;
    private Long userId;
    private String sourceType;  // DRAW, EDIT
    private String brand;       // 品牌（画板模式下可能为空）
    private Integer colorCount;  // 色数（可能为空）
    private String name;         // 图纸名称
    private Integer gridSize;   // 尺寸
    private String rgbData;      // 原始RGB数组（画板模式下可能为空）
    private String gridData;     // 色号索引数组
    private String colorPalette; // 颜色表
    private Long boxId;         // 关联图纸箱ID
    
    // V6.0 新增字段
    private String pixelData;    // 像素数据JSON（可选，用于优化大数据传输）
    private String mappedPixelData; // 前端主格式JSON（API字段，映射到pixelData）
    private String colorMapping; // 颜色映射JSON
    
    private LocalDateTime expiresAt; // 过期时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
