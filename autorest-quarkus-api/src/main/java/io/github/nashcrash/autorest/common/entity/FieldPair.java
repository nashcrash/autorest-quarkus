package io.github.nashcrash.autorest.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public final class FieldPair {
    private String originalField;
    private String targetField;

    public static FieldPair of(String originalField, String targetField) {
        return new FieldPair(originalField, targetField);
    }
}
