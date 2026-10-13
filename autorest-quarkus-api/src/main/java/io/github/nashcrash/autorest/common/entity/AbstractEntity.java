package io.github.nashcrash.autorest.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
public abstract class AbstractEntity {
    /**
     * Stable identifier of the entity over time: it is the same on every version of a historical entity,
     * and it is the reference to use from other entities. Unlike the technical id, it is never regenerated.
     */
    private String businessKey;
    private String insertionUser;
    private String lastModifiedUser;
    private Instant insertionDate;
    private Instant lastModifiedDate;
}
