package io.github.nashcrash.autorest.common.validator;

import io.github.nashcrash.autorest.common.exception.CustomException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ParamConverter;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultipleDateTimeFormatParser implements ParamConverter<Date> {
    public static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final String DEFAULT_MESSAGE = "Invalid date format: {0}";
    private String[] patterns;
    private String message;

    public MultipleDateTimeFormatParser(String[] patterns, String message) {
        this.patterns = patterns;
        this.message = message;
    }

    public static Date parseDate(String referenceDate, String[] patterns, String message) {
        if (referenceDate == null) return null;
        patterns = (patterns == null || patterns.length < 1) ? new String[]{ISO_PATTERN} : patterns;
        message = (message == null || message.isBlank()) ? DEFAULT_MESSAGE : message;
        Date refDate = null;
        for (String pattern : patterns) {
            try {
                refDate = parseDate(referenceDate, pattern);
                break;
            } catch (ParseException ignore) {
            }
        }
        if (refDate == null) {
            throw new CustomException(Response.Status.BAD_REQUEST, MessageFormat.format(message, referenceDate));
        }
        return refDate;
    }

    private static final Pattern ZONE_PATTERN = Pattern.compile("@Z\\(([-+:/0-9a-zA-Z_]+)\\)");
    private static final Pattern TIME_PATTERN = Pattern.compile("@T\\(([0-9]{2}):([0-9]{2})(?::([0-9]{2}))?(?:\\.([0-9]{3}))?\\)");

    /**
     * Parse a date applying optional custom extensions:
     * - @Z(zoneId)
     * - @T(HH:mm[:ss][.SSS])
     */
    public static Date parseDate(String referenceDate, String pattern) throws ParseException {
        String effectivePattern = pattern;

        ZoneId zoneId = null;
        Matcher zoneMatcher = ZONE_PATTERN.matcher(pattern);
        if (zoneMatcher.find()) {
            zoneId = ZoneId.of(zoneMatcher.group(1));
            effectivePattern = effectivePattern.replace(zoneMatcher.group(0), "");
        }
        Matcher timeMatcher = TIME_PATTERN.matcher(pattern);
        if (timeMatcher.find()) {
            effectivePattern = effectivePattern.replace(timeMatcher.group(0), "");
        }
        effectivePattern = effectivePattern.trim();

        SimpleDateFormat sdf = new SimpleDateFormat(effectivePattern);
        if (zoneId != null) {
            sdf.setTimeZone(TimeZone.getTimeZone(zoneId));
        }
        Date parsedDate = sdf.parse(referenceDate);
        if (!timeMatcher.find(0)) {
            return parsedDate;
        }
        int hour = Integer.parseInt(timeMatcher.group(1));
        int minute = Integer.parseInt(timeMatcher.group(2));
        int second = timeMatcher.group(3) != null ? Integer.parseInt(timeMatcher.group(3)): 0;
        int milli = timeMatcher.group(4) != null? Integer.parseInt(timeMatcher.group(4)): 0;

        ZoneId effectiveZone = zoneId != null ? zoneId : ZoneId.systemDefault();
        ZonedDateTime zdt = parsedDate.toInstant().atZone(effectiveZone).with(LocalTime.of(hour, minute, second, milli * 1_000_000));
        return Date.from(zdt.toInstant());
    }

    private static Date combineToDate(String dateStr, String timeStr, ZoneId zoneId) {
        DateTimeFormatter timeFormatter = new DateTimeFormatterBuilder()
                .appendPattern("HH:mm:ss")
                .appendFraction(ChronoField.MILLI_OF_SECOND, 0, 3, true)
                .toFormatter();
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalTime time = LocalTime.parse(timeStr, timeFormatter);
        ZonedDateTime zonedDateTime = ZonedDateTime.of(date, time, zoneId);
        return Date.from(zonedDateTime.toInstant());
    }

    @Override
    public Date fromString(String s) {
        return parseDate(s, patterns, message);
    }

    @Override
    public String toString(Date date) {
        return new SimpleDateFormat(ISO_PATTERN).format(date);
    }
}
