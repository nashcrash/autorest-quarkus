package io.github.nashcrash.autorest.common.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@MappedSuperclass
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
public abstract class AbstractEntityHistoricalSQL extends AbstractEntitySQL implements AbstractEntityHistorical {
    private Instant startValidityDate;
    private Instant endValidityDate;
}
