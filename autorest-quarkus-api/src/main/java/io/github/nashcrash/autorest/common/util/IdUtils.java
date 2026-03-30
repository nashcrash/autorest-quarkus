package io.github.nashcrash.autorest.common.util;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class IdUtils {
    public static String generateId(Object... keys) {
        List<String> stringKeys = new ArrayList<>();
        for (Object element : keys) {
            if (element instanceof Instant instant) {
                element = instant.getEpochSecond() + "_" + instant.getNano();
            } else if (element instanceof ZonedDateTime zonedDateTime) {
                element = zonedDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
            } else if (element instanceof LocalDateTime localDateTime) {
                element = localDateTime.format(DateTimeFormatter.ISO_DATE_TIME);
            } else if (element instanceof LocalDate localDate) {
                element = localDate.format(DateTimeFormatter.ISO_DATE);
            }
            stringKeys.add(String.valueOf(element));
        }
        return Base64.getEncoder().encodeToString(StringUtils.join(stringKeys, "|").getBytes(StandardCharsets.UTF_8));
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }
}
