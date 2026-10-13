package io.github.nashcrash.autorest.historicalentity;

import io.github.nashcrash.autorest.common.entity.AbstractHistoricalDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
@RegisterForReflection
public class HistoricalEntityDTO extends AbstractHistoricalDTO {
    private String code;
    private String description;
    private Double value;
}
