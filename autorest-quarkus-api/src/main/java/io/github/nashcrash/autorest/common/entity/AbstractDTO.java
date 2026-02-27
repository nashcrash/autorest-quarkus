package io.github.nashcrash.autorest.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
public abstract class AbstractDTO {
    private String id;
    private String insertionUser;
    private String lastModifiedUser;
    private Instant insertionDate;
    private Instant lastModifiedDate;
}
