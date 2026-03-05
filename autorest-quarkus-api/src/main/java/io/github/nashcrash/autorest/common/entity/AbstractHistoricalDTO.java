package io.github.nashcrash.autorest.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
public abstract class AbstractHistoricalDTO extends AbstractDTO {
    private Instant startValidityDate;
    private Instant endValidityDate;
}
