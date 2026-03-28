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
                String[] pattern_parts = pattern.split("@T");
                refDate = new SimpleDateFormat(pattern_parts[0]).parse(referenceDate);
                if (pattern_parts.length>1) {
                    String day=new SimpleDateFormat("yyyy-MM-dd").format(refDate);
                    ZoneId zoneId = (pattern_parts.length>2) ? ZoneId.of(pattern_parts[2]) : ZoneId.systemDefault();
                    refDate = combineToDate(day, pattern_parts[1], zoneId);
                }
                break;
            } catch (ParseException ignore) {
            }
        }
        if (refDate == null) {
            throw new CustomException(Response.Status.BAD_REQUEST, MessageFormat.format(message, referenceDate));
        }
        return refDate;
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
