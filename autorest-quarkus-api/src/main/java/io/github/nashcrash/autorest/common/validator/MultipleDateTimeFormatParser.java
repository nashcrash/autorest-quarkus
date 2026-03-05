package io.github.nashcrash.autorest.common.validator;

import io.github.nashcrash.autorest.common.exception.CustomException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ParamConverter;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
        if (referenceDate==null) return null;
        patterns = (patterns == null || patterns.length < 1) ? new String[] {ISO_PATTERN} : patterns;
        message = (message == null || message.isBlank()) ? DEFAULT_MESSAGE : message;
        Date refDate = null;
        for (String pattern : patterns) {
            try {
                refDate = new SimpleDateFormat(pattern).parse(referenceDate);
                break;
            } catch (ParseException ignore) {
            }
        }
        if (refDate == null) {
            throw new CustomException(Response.Status.BAD_REQUEST, MessageFormat.format(message, referenceDate));
        }
        return refDate;
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
