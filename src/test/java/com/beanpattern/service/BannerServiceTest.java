package com.beanpattern.service;

import com.beanpattern.entity.BannerEntity;
import com.beanpattern.entity.BpUserGift;
import com.beanpattern.mapper.BannerClaimLogMapper;
import com.beanpattern.mapper.BannerMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BannerServiceTest {

    @Mock
    private BannerMapper bannerMapper;
    @Mock
    private BannerClaimLogMapper claimLogMapper;
    @Mock
    private GiftPackageService giftPackageService;

    @InjectMocks
    private BannerService bannerService;

    @Test
    void claimBannerGiftUsesDatabaseLockAroundLimitCheckAndGrant() {
        Long userId = 7L;
        Long bannerId = 8L;
        BannerEntity banner = activeClaimBanner(bannerId, "{\"giftPackageCode\":\"new_user\",\"limit\":\"ONCE\",\"banner_code\":\"new_user\"}");
        BpUserGift gift = BpUserGift.builder()
                .id(11L)
                .userId(userId)
                .giftType("GIFT_PACKAGE")
                .giftName("New user")
                .build();

        when(bannerMapper.findById(bannerId)).thenReturn(banner);
        when(claimLogMapper.acquireLock(anyString(), eq(5))).thenReturn(1);
        when(claimLogMapper.countByUserAndBanner(userId, "new_user")).thenReturn(0);
        when(giftPackageService.grantPackageToUser(userId, "new_user", "BANNER:new_user")).thenReturn(gift);

        Map<String, Object> result = bannerService.claimBannerGift(userId, bannerId);

        assertEquals(true, result.get("success"));
        assertEquals(11L, result.get("giftId"));
        verify(claimLogMapper).insert(any());
        verify(claimLogMapper).releaseLock(anyString());
    }

    @Test
    void claimBannerGiftDoesNotGrantWhenClaimLockIsBusy() {
        Long userId = 7L;
        Long bannerId = 8L;
        BannerEntity banner = activeClaimBanner(bannerId, "{\"giftPackageCode\":\"new_user\",\"limit\":\"ONCE\",\"banner_code\":\"new_user\"}");

        when(bannerMapper.findById(bannerId)).thenReturn(banner);
        when(claimLogMapper.acquireLock(anyString(), eq(5))).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> bannerService.claimBannerGift(userId, bannerId));

        verify(giftPackageService, never()).grantPackageToUser(any(), anyString(), anyString());
        verify(claimLogMapper, never()).insert(any());
        verify(claimLogMapper, never()).releaseLock(anyString());
    }

    private BannerEntity activeClaimBanner(Long id, String actionConfig) {
        BannerEntity banner = new BannerEntity();
        banner.setId(id);
        banner.setStatus(1);
        banner.setActionType("CLAIM_GIFT");
        banner.setActionConfig(actionConfig);
        return banner;
    }
}
