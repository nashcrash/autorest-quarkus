package io.github.nashcrash.autorest.entity;

import io.github.nashcrash.autorest.common.entity.AbstractDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
@RegisterForReflection
public class EntityDTO extends AbstractDTO {
    private String eventCode;
    private String transactionType;
    private Double value;
    private List<SubEntity> movements;
}
