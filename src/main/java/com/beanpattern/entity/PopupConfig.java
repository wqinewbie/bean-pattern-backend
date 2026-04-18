package com.beanpattern.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PopupConfig {
    private Long id;
    private String key;
    private String title;
    private String content;
    private String imageUrl;
    private String buttonText;
    private String buttonUrl;
    private Integer priority;
    private Integer enabled;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer showInterval;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
