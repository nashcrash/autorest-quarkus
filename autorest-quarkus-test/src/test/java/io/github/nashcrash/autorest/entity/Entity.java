package io.github.nashcrash.autorest.entity;

import io.github.nashcrash.autorest.api.Aggregate;
import io.github.nashcrash.autorest.api.Reactive;
import io.github.nashcrash.autorest.api.ResourceAPI;
import io.github.nashcrash.autorest.api.ResourceClient;
import io.github.nashcrash.autorest.common.entity.AbstractEntityMongo;
import io.github.nashcrash.autorest.common.entity.AccumulatorType;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.List;

@ResourceAPI(
        basePath = "/entity",
        dto = EntityDTO.class,
        idFields = {"eventCode"}
)
@Aggregate(
        name = "byTransactionType",
        path = "/transactiontype",
        dto = TransactionTypeDTO.class,
        groupBy = {
                @Aggregate.AggregateFieldPair(originalField = "transactionType", targetField = "transactionType"),
                @Aggregate.AggregateFieldPair(originalField = "value", targetField = "value")
        }
)
@Aggregate(
        name = "byTransactionTypeSum",
        path = "/transactiontypesum",
        dto = TransactionTypeDTO.class,
        groupBy = {
                @Aggregate.AggregateFieldPair(originalField = "transactionType", targetField = "transactionType")
        },
        aggregateBy = {
                @Aggregate.AggregateMapEntry(
                        accumulator = AccumulatorType.SUM,
                        value = @Aggregate.AggregateFieldPair(originalField = "value", targetField = "value")
                )
        }
)
@Aggregate(
        name = "selectSubEntity",
        path = "/selectsubentity",
        dto = SubEntity.class,
        unwind = @Aggregate.AggregateUnwindField(originalField = "movements"),
        groupBy = {
                @Aggregate.AggregateFieldPair(originalField = "transactionType", targetField = "transactionType"),
                @Aggregate.AggregateFieldPair(originalField = "value", targetField = "value")
        }
)
@Reactive
@ResourceClient(configKey = "microservice")
@MongoEntity(collection = "entity")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@SuperBuilder(toBuilder = true)
public class Entity extends AbstractEntityMongo {
    private String eventCode;
    private String transactionType;
    private Double value;
    private List<SubEntity> movements;

}
