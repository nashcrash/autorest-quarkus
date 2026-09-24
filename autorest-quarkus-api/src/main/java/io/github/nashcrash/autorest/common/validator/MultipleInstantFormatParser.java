package io.github.nashcrash.autorest.common.validator;

import jakarta.ws.rs.ext.ParamConverter;

import java.time.Instant;
import java.util.Date;

public class MultipleInstantFormatParser implements ParamConverter<Instant> {
    public static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final String DEFAULT_MESSAGE = "Invalid date format: {0}";
    private String[] patterns;
    private String serializePattern;
    private String message;

    public MultipleInstantFormatParser(String[] patterns, String serializePattern, String message) {
        this.patterns = patterns;
        this.serializePattern = serializePattern;
        this.message = message;
    }

    public static Instant parseDate(String referenceDate, String[] patterns, String message) {
        return MultipleDateTimeFormatParser.parseDate(referenceDate, patterns, message).toInstant();
    }

    @Override
    public Instant fromString(String s) {
        return parseDate(s, patterns, message==null ? DEFAULT_MESSAGE: message);
    }

    @Override
    public String toString(Instant date) {
        return new MultipleDateTimeFormatParser(patterns, serializePattern, message).toString(Date.from(date));
    }
}
