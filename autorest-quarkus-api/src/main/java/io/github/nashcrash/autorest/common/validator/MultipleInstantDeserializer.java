package io.github.nashcrash.autorest.common.validator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
public class MultipleInstantDeserializer extends JsonDeserializer<Instant> implements ContextualDeserializer {
    private String[] patterns;
    private String message;

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        MultipleDateTimeFormat ann = property.getAnnotation(MultipleDateTimeFormat.class);
        if (ann != null) {
            patterns = ann.patterns();
            message = ann.message();
        }
        return new MultipleInstantDeserializer(patterns, message);
    }

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        return MultipleInstantFormatParser.parseDate(value, patterns, message);
    }
}