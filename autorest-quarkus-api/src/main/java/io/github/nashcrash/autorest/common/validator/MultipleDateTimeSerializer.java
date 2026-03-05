package io.github.nashcrash.autorest.common.validator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.Date;

@AllArgsConstructor
public class MultipleDateTimeSerializer extends JsonSerializer<Date> implements ContextualSerializer {
    private String[] patterns;
    private String message;

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty property) throws JsonMappingException {
        MultipleDateTimeFormat ann = property.getAnnotation(MultipleDateTimeFormat.class);
        String[] patterns = null;
        String message = null;
        if (ann != null) {
            patterns = ann.patterns();
            message = ann.message();
        }
        return new MultipleDateTimeSerializer(patterns, message);
    }

    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (date == null) {
            jsonGenerator.writeNull();
            return;
        }
        jsonGenerator.writeString(new MultipleDateTimeFormatParser(patterns, message).toString(date));
    }
}
