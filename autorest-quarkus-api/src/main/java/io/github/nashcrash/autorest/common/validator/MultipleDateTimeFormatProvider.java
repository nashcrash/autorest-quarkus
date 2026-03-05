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

@Provider
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MultipleDateTimeFormatProvider implements ParamConverterProvider {
    protected String[] default_patterns = {MultipleDateTimeFormatParser.ISO_PATTERN};

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof MultipleDateTimeFormat multipleDateTimeFormat && rawType.equals(String.class)) {
                String[] patterns = multipleDateTimeFormat.patterns();
                String message = multipleDateTimeFormat.message();
                return (ParamConverter<T>) new MultipleDateTimeFormatParser(patterns, message);
            }
        }
        return null; // Fallback to default Quarkus converters
    }
}
