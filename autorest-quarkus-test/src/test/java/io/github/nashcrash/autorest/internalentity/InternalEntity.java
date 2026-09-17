package io.github.nashcrash.autorest.internalentity;

import io.github.nashcrash.autorest.api.Reactive;
import io.github.nashcrash.autorest.api.ResourceAPI;
import io.github.nashcrash.autorest.common.entity.AbstractEntityMongo;
import io.github.nashcrash.autorest.entity.EntityDTO;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@ResourceAPI(
        basePath = "/internal_entity",
        dto = InternalEntityDTO.class,
        idFields = {"eventCode"},
        generate = @ResourceAPI.ClassesToGenerate(resource = false)
)
@MongoEntity(collection = "internal_entity")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
public class InternalEntity extends AbstractEntityMongo {
    private String eventCode;
    private String transactionType;
    private Double value;
}
