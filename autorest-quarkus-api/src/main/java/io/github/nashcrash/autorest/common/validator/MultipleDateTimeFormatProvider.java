package io.github.nashcrash.autorest.common.validator;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Date;

@Provider
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultipleDateTimeFormatProvider implements ParamConverterProvider {
    protected String[] default_patterns = {MultipleDateTimeFormatParser.ISO_PATTERN};

    @SuppressWarnings("unchecked")
    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof MultipleDateTimeFormat multipleDateTimeFormat) {
                String[] patterns = multipleDateTimeFormat.patterns();
                String message = multipleDateTimeFormat.message();
                if (rawType.equals(Date.class)) {
                    return (ParamConverter<T>) new MultipleDateTimeFormatParser(patterns, message);
                } else if (rawType.equals(Instant.class)) {
                    return (ParamConverter<T>) new MultipleInstantFormatParser(patterns, message);
                }
            }
        }
        return null; // Fallback to default Quarkus converters
    }
}
