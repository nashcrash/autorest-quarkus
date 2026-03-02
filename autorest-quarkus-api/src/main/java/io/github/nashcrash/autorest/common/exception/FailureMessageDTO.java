package io.github.nashcrash.autorest.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
@Jacksonized
public class FailureMessageDTO {
    private Instant timestamp;
    private Integer status;
    private String error;
    @Builder.Default
    private String message = "";
    private Map<String, Object> extra;
    private String path;
}