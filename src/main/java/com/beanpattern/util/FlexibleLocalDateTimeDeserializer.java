package com.beanpattern.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter LOCAL_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Shanghai");

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String text = value.trim();
        try {
            return LocalDateTime.parse(text, LOCAL_DATE_TIME);
        } catch (DateTimeParseException ignored) {
            // Try ISO variants below.
        }

        try {
            return OffsetDateTime.parse(text, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .atZoneSameInstant(APP_ZONE)
                    .toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // Try local ISO datetime below.
        }

        try {
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException ex) {
            throw context.weirdStringException(text, LocalDateTime.class, "expected yyyy-MM-dd HH:mm:ss or ISO date-time");
        }
    }
}
