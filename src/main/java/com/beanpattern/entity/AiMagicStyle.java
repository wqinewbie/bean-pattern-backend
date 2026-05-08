package com.beanpattern.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * AI魔法风格配置实体
 */
@Data
public class AiMagicStyle {
    private Long id;
    private String name;
    private String icon;
    private String tag;
    private String description;
    private String promptTemplate;
    private Integer sortOrder;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
