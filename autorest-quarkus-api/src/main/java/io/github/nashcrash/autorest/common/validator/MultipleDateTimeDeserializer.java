package io.github.nashcrash.autorest.common.validator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Date;

@AllArgsConstructor
public class MultipleDateTimeDeserializer extends JsonDeserializer<Date> implements ContextualDeserializer {
    private String[] patterns;
    private String message;
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        MultipleDateTimeFormat ann = property.getAnnotation(MultipleDateTimeFormat.class);
        String[] patterns = null;
        String message = null;
        if (ann != null) {
            patterns = ann.patterns();
            message = ann.message();
        }
        return new MultipleDateTimeDeserializer(patterns, message);
    }

    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        return MultipleDateTimeFormatParser.parseDate(value, patterns, message);
    }
}