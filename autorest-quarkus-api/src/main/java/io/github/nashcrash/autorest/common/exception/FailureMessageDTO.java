package io.github.nashcrash.autorest.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@Builder(toBuilder = true)
public class FailureMessageDTO {
    private Instant timestamp;
    private Integer status;
    private String error;
    @Builder.Default
    private String message = "";
    private Map<String, Object> extra;
    private String path;
}