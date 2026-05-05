package io.github.nashcrash.autorest.common.util;

import com.mongodb.client.model.*;
import io.github.nashcrash.autorest.common.entity.AccumulatorType;
import io.github.nashcrash.autorest.common.entity.FieldPair;
import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.quarkus.panache.common.Sort;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PipelineUtils {

    public static List<Bson> aggregate(List<FieldPair> groupBy, Map<AccumulatorType, FieldPair> aggregateBy, String unwindFields, FindDTO findDTO) {
        List<Bson> pipeline = new ArrayList<>();

        if (StringUtils.isNotEmpty(findDTO.getQuery())) {
            BsonDocument query = BsonDocument.parse(findDTO.getQuery());
            pipeline.add(Aggregates.match(query)); //Add filter
        }

        if (unwindFields != null && !unwindFields.isEmpty()) {
            String fieldPath = unwindFields.startsWith("$") ? unwindFields : "$" + unwindFields;
            pipeline.add(Aggregates.unwind(fieldPath));
        }

        List<Bson> projections = new ArrayList<>();
        projections.add(Projections.excludeId());
        if (groupBy != null && !groupBy.isEmpty()) {

            projections.add(Projections.excludeId());
            Document groupKey = new Document();
            for (FieldPair fieldPair : groupBy) {
                groupKey.append(fieldPair.getTargetField(), "$" + fieldPair.getOriginalField());
                projections.add(Projections.computed(fieldPair.getTargetField(), "$_id." + fieldPair.getTargetField()));
            }

            if (aggregateBy != null && !aggregateBy.isEmpty()) {
                List<BsonField> fieldAccumulators = new ArrayList<>();
                for (Map.Entry<AccumulatorType, FieldPair> entry : aggregateBy.entrySet()) {
                    fieldAccumulators.add(generateAccumulator(entry.getKey(), entry.getValue()));
                    projections.add(Projections.include(entry.getValue().getTargetField()));
                }
                pipeline.add(Aggregates.group(groupKey, fieldAccumulators)); //Add aggregation
            } else {
                pipeline.add(Aggregates.group(groupKey)); //Add aggregation
            }
        } else if (aggregateBy != null) {
            FieldPair fieldPair = aggregateBy.get(AccumulatorType.PROJECTION);
            if (fieldPair != null && fieldPair.getTargetField() != null) {
                Arrays.stream(fieldPair.getTargetField().split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .forEach(s -> projections.add(Projections.include(s)));
            } else {
                throw new IllegalArgumentException("For aggregation without grouping, a projection field pair with target field must be provided");
            }
        }

        pipeline.add(Aggregates.project(Projections.fields(projections))); //Add projection
        pipeline.add(Aggregates.sort(getBsonSort(findDTO)));
        pipeline.add(Aggregates.skip(findDTO.getPage() * findDTO.getLimit()));
        pipeline.add(Aggregates.limit(findDTO.getLimit()));

        return pipeline;
    }

    private static BsonField generateAccumulator(AccumulatorType key, FieldPair value) {
        String fieldReference = "$" + value.getOriginalField();
        String targetField = value.getTargetField();
        return switch (key) {
            case FIRST -> Accumulators.first(targetField, fieldReference);
            case LAST -> Accumulators.last(targetField, fieldReference);
            case SUM -> Accumulators.sum(targetField, fieldReference);
            case AVG -> Accumulators.avg(targetField, fieldReference);
            case MIN -> Accumulators.min(targetField, fieldReference);
            case MAX -> Accumulators.max(targetField, fieldReference);
            case COUNT -> Accumulators.sum(targetField, 1);
            case PROJECTION -> throw new IllegalArgumentException("Accumulator type PROJECTION is used only without grouping");
            case null -> throw new IllegalArgumentException("Accumulator type cannot be null");
        };
    }

    public static Sort getSort(FindDTO findDTO) {
        Sort sort = Sort.empty();
        if (findDTO.getOrderBy() != null && findDTO.getOrderDirection() == null)
            throw new CustomException(Response.Status.BAD_REQUEST, FindDTO.EM_MISSING_ORDER_DIRECTION);
        if (findDTO.getOrderBy() != null && findDTO.getOrderBy().length != findDTO.getOrderDirection().length)
            throw new CustomException(Response.Status.BAD_REQUEST, FindDTO.EM_INSUFFICIENT_ORDER_DIRECTION);
        if (findDTO.getOrderBy() != null) {
            for (int i = 0; i < findDTO.getOrderBy().length; i++) {
                sort = sort.and(findDTO.getOrderBy()[i], FindDTO.ORDER_DESC.equalsIgnoreCase(findDTO.getOrderDirection()[i]) ? Sort.Direction.Descending : Sort.Direction.Ascending);
            }
        }
        return sort;
    }

    public static Bson getBsonSort(FindDTO findDTO) {
        Bson defaultSort = Sorts.ascending("_id");
        if (findDTO.getOrderBy() == null || findDTO.getOrderBy().length == 0) {
            return defaultSort;
        }
        if (findDTO.getOrderDirection() == null) {
            throw new CustomException(Response.Status.BAD_REQUEST, FindDTO.EM_MISSING_ORDER_DIRECTION);
        }
        if (findDTO.getOrderBy().length != findDTO.getOrderDirection().length) {
            throw new CustomException(Response.Status.BAD_REQUEST, FindDTO.EM_INSUFFICIENT_ORDER_DIRECTION);
        }
        List<Bson> sorts = new ArrayList<>();
        for (int i = 0; i < findDTO.getOrderBy().length; i++) {
            sorts.add(FindDTO.ORDER_DESC.equalsIgnoreCase(findDTO.getOrderDirection()[i])
                    ? Sorts.descending(findDTO.getOrderBy()[i])
                    : Sorts.ascending(findDTO.getOrderBy()[i]));
        }
        return Sorts.orderBy(sorts);
    }
}
