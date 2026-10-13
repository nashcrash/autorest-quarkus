package io.github.nashcrash.autorest.historicalentity;

import io.github.nashcrash.autorest.api.ResourceAPI;
import io.github.nashcrash.autorest.common.entity.AbstractEntityHistoricalMongo;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

/**
 * Historical entity on the blocking MongoDB stack, with a derived business key.
 * Declaring businessKeyFields keeps the key predictable (Base64 of the code, e.g. H01 -> SDAx),
 * which is what makes the YAML test cases able to address the entity: the test engine cannot
 * capture a server-generated identifier from a previous response.
 */
@ResourceAPI(
        basePath = "/historical_entity",
        dto = HistoricalEntityDTO.class,
        businessKeyFields = {"code"}
)
@MongoEntity(collection = "historical_entity")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
public class HistoricalEntity extends AbstractEntityHistoricalMongo {
    private String code;
    private String description;
    private Double value;
}
