package io.github.nashcrash.autorest.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public final class FieldMap {
    private AccumulatorType type;
    private String originalField;
    private String targetField;

    public static FieldMap of(AccumulatorType type, String originalField, String targetField) {
        return new FieldMap(type, originalField, targetField);
    }
}
