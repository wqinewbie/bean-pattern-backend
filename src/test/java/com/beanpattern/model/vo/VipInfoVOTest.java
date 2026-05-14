package com.beanpattern.model.vo;

import com.beanpattern.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VipInfoVOTest {

    @Test
    void treatsFutureExpireAtAsVipWhenStoredLevelIsPositive() {
        UserEntity user = new UserEntity();
        user.setVipLevel(1);
        user.setVipExpireAt(LocalDateTime.now().plusDays(3));

        VipInfoVO vo = VipInfoVO.from(user, user.getVipLevel());

        assertTrue(vo.getIsVip());
        assertEquals(1, vo.getVipLevel());
    }

    @Test
    void treatsFutureExpireAtAsNonVipWhenStoredLevelIsZero() {
        UserEntity user = new UserEntity();
        user.setVipLevel(0);
        user.setVipExpireAt(LocalDateTime.now().plusDays(3));

        VipInfoVO vo = VipInfoVO.from(user, user.getVipLevel());

        assertFalse(vo.getIsVip());
        assertEquals(0, vo.getVipLevel());
    }

    @Test
    void treatsExpiredVipAsNonVipEvenWhenStoredLevelIsPositive() {
        UserEntity user = new UserEntity();
        user.setVipLevel(1);
        user.setVipExpireAt(LocalDateTime.now().minusDays(1));

        VipInfoVO vo = VipInfoVO.from(user, user.getVipLevel());

        assertFalse(vo.getIsVip());
        assertEquals(1, vo.getVipLevel());
    }
}
