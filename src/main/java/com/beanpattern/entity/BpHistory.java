package com.beanpattern.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BpHistory {
    private Long id;
    private Long userId;
    private String taskId;
    private String sourceType;
    private String brand;
    private Integer colorCount;
    private String name;
    private Integer gridSize;
    private Long boxId;
    private String sourceUrl;
    private String mappedPixelData;
    private String aiStyle;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
