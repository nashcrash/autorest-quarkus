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
    /**
     * Stable identifier of the entity over time, shared by every version of a historical entity.
     * Use it to reference the entity; the id identifies a single version.
     */
    private String businessKey;
    private String insertionUser;
    private String lastModifiedUser;
    private Instant insertionDate;
    private Instant lastModifiedDate;
}
