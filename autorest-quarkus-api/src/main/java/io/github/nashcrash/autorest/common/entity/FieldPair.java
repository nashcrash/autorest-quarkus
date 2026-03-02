package io.github.nashcrash.autorest.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public final class FieldPair {
    private String originalField;
    private String targetField;

    public static FieldPair of(String originalField, String targetField) {
        return new FieldPair(originalField, targetField);
    }
}
