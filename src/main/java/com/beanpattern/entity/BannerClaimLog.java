package com.beanpattern.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BannerClaimLog {
    private Long id;
    private Long userId;
    private Long bannerId;
    private String bannerCode;
    private String giftType;
    private Integer giftValue;
    private LocalDate claimDate;
    private LocalDateTime createdAt;
}
