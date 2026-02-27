package io.github.nashcrash.autorest.common.util;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

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
            if (element instanceof Instant) {
                element = DateTimeFormatter.ISO_DATE_TIME.format((Instant) element);
            } else if (element instanceof ZonedDateTime) {
                element = ((ZonedDateTime) element).format(DateTimeFormatter.ISO_DATE_TIME);
            } else if (element instanceof LocalDateTime) {
                element = ((LocalDateTime) element).format(DateTimeFormatter.ISO_DATE_TIME);
            } else if (element instanceof LocalDate) {
                element = ((LocalDate) element).format(DateTimeFormatter.ISO_DATE);
            }
            stringKeys.add(String.valueOf(element));
        }
        return Base64.getEncoder().encodeToString(StringUtils.join(stringKeys, "|").getBytes(StandardCharsets.UTF_8));
    }

    public static String objectIdToString(ObjectId objectId) {
        return objectId.toString();
    }

    public static ObjectId stringToObjectId(String objectIdAsString) {
        if (objectIdAsString != null && !objectIdAsString.isBlank())
            return new ObjectId(objectIdAsString);
        return null;
    }

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }
}
