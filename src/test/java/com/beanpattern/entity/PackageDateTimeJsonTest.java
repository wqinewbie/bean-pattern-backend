package com.beanpattern.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PackageDateTimeJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void vipPackageAcceptsAdminDateTimeString() throws Exception {
        VipPackage pkg = objectMapper.readValue(
                "{\"shelfStartTime\":\"2026-06-03 18:00:00\",\"shelfEndTime\":\"2026-06-04 09:30:00\"}",
                VipPackage.class);

        assertEquals(LocalDateTime.of(2026, 6, 3, 18, 0), pkg.getShelfStartTime());
        assertEquals(LocalDateTime.of(2026, 6, 4, 9, 30), pkg.getShelfEndTime());
    }

    @Test
    void cardPackageConvertsUtcIsoDateTimeToShanghaiLocalTime() throws Exception {
        CardPackage pkg = objectMapper.readValue(
                "{\"shelfStartTime\":\"2026-06-03T10:00:00Z\"}",
                CardPackage.class);

        assertEquals(LocalDateTime.of(2026, 6, 3, 18, 0), pkg.getShelfStartTime());
    }

    @Test
    void cardPackageTreatsBlankDateTimeAsNull() throws Exception {
        CardPackage pkg = objectMapper.readValue(
                "{\"shelfStartTime\":\"\",\"shelfEndTime\":null}",
                CardPackage.class);

        assertEquals(null, pkg.getShelfStartTime());
        assertEquals(null, pkg.getShelfEndTime());
    }
}
