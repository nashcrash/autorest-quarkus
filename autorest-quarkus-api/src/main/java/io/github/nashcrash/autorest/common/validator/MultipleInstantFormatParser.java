package io.github.nashcrash.autorest.common.validator;

import jakarta.ws.rs.ext.ParamConverter;

import java.text.SimpleDateFormat;
import java.time.Instant;

public class MultipleInstantFormatParser implements ParamConverter<Instant> {
    public static final String ISO_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final String DEFAULT_MESSAGE = "Invalid date format: {0}";
    private String[] patterns;
    private String message;

    public MultipleInstantFormatParser(String[] patterns, String message) {
        this.patterns = patterns;
        this.message = message;
    }

    public static Instant parseDate(String referenceDate, String[] patterns, String message) {
        return MultipleDateTimeFormatParser.parseDate(referenceDate, patterns, message).toInstant();
    }

    @Override
    public Instant fromString(String s) {
        return parseDate(s, patterns, message);
    }

    @Override
    public String toString(Instant date) {
        return new SimpleDateFormat(ISO_PATTERN).format(date);
    }
}
