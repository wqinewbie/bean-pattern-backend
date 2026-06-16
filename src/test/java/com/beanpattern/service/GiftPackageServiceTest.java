package com.beanpattern.service;

import com.beanpattern.entity.BpUserGift;
import com.beanpattern.entity.GiftPackage;
import com.beanpattern.mapper.BpUserGiftMapper;
import com.beanpattern.mapper.GiftPackageMapper;
import com.beanpattern.mapper.GiftTypeConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GiftPackageServiceTest {

    @Mock
    private GiftPackageMapper giftPackageMapper;
    @Mock
    private GiftTypeConfigMapper giftTypeConfigMapper;
    @Mock
    private BpUserGiftMapper bpUserGiftMapper;
    @Mock
    private UserService userService;
    @Mock
    private AiQuotaLogService aiQuotaLogService;

    @InjectMocks
    private GiftPackageService giftPackageService;

    @Test
    void redeemPackageGiftResolvesNewTableGiftByGiftIdWhenSourceIsBusinessSource() {
        Long userId = 7L;
        Long userGiftId = 11L;
        Long giftPackageId = 23L;

        BpUserGift userGift = BpUserGift.builder()
                .id(userGiftId)
                .userId(userId)
                .giftId(giftPackageId)
                .giftType("GIFT_PACKAGE")
                .source("CHECKIN:2026-05-20")
                .status("UNUSED")
                .expireAt(LocalDateTime.now().plusDays(1))
                .build();

        GiftPackage giftPackage = new GiftPackage();
        giftPackage.setId(giftPackageId);
        giftPackage.setPackageCode("CHECKIN_DAILY");
        giftPackage.setName("Daily checkin");
        giftPackage.setStatus(1);
        giftPackage.setExpireDays(30);
        giftPackage.setItemsJson("[{\"type\":\"AI_QUOTA\",\"value\":3}]");

        when(bpUserGiftMapper.findById(userGiftId)).thenReturn(userGift);
        when(giftPackageMapper.findById(giftPackageId)).thenReturn(giftPackage);
        when(bpUserGiftMapper.use(userGiftId, null)).thenReturn(1);

        giftPackageService.redeemPackageGift(userId, userGiftId);

        verify(giftPackageMapper, never()).findByCode(any());
        verify(giftPackageMapper).findById(giftPackageId);
        verify(bpUserGiftMapper).use(userGiftId, null);
        verify(userService).addAiQuota(userId, 3);
        verify(aiQuotaLogService).logChange(
                eq(userId),
                eq("GIFT"),
                eq(3),
                eq("GIFT_PACKAGE"),
                eq(String.valueOf(userGiftId)),
                eq("Redeem gift package: Daily checkin"));
    }

    @Test
    void redeemPackageGiftCannotGrantBenefitsTwiceForSameUserGift() {
        Long userId = 7L;
        Long userGiftId = 11L;
        Long giftPackageId = 23L;

        BpUserGift unusedGift = BpUserGift.builder()
                .id(userGiftId)
                .userId(userId)
                .giftId(giftPackageId)
                .giftType("GIFT_PACKAGE")
                .source("BANNER:home_gift")
                .status("UNUSED")
                .expireAt(LocalDateTime.now().plusDays(1))
                .build();
        BpUserGift usedGift = BpUserGift.builder()
                .id(userGiftId)
                .userId(userId)
                .giftId(giftPackageId)
                .giftType("GIFT_PACKAGE")
                .source("BANNER:home_gift")
                .status("USED")
                .expireAt(LocalDateTime.now().plusDays(1))
                .build();

        GiftPackage giftPackage = new GiftPackage();
        giftPackage.setId(giftPackageId);
        giftPackage.setPackageCode("HOME_BANNER");
        giftPackage.setName("Home banner gift");
        giftPackage.setStatus(1);
        giftPackage.setExpireDays(30);
        giftPackage.setItemsJson("[{\"type\":\"AI_QUOTA\",\"value\":3}]");

        when(bpUserGiftMapper.findById(userGiftId)).thenReturn(unusedGift, usedGift);
        when(giftPackageMapper.findById(giftPackageId)).thenReturn(giftPackage);
        when(bpUserGiftMapper.use(userGiftId, null)).thenReturn(1);

        giftPackageService.redeemPackageGift(userId, userGiftId);
        assertThrows(IllegalStateException.class,
                () -> giftPackageService.redeemPackageGift(userId, userGiftId));

        verify(bpUserGiftMapper, times(1)).use(userGiftId, null);
        verify(userService, times(1)).addAiQuota(userId, 3);
        verify(aiQuotaLogService, times(1)).logChange(
                eq(userId),
                eq("GIFT"),
                eq(3),
                eq("GIFT_PACKAGE"),
                eq(String.valueOf(userGiftId)),
                eq("Redeem gift package: Home banner gift"));
    }
}
