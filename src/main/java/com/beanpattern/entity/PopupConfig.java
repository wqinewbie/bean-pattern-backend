package com.beanpattern.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@Data
public class PopupConfig {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private Long id;
    private String key;
    private String title;
    private String content;
    private String imageUrl;
    private String buttonText;
    private String buttonUrl;
    private Integer priority;
    private Integer enabled;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime startTime;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime endTime;
    private Integer showInterval;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime createdAt;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime updatedAt;
}
