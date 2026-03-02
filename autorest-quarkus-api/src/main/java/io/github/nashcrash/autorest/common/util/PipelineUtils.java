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
import java.util.List;
import java.util.Map;

public class PipelineUtils {

    public static List<Bson> aggregate(List<FieldPair> groupBy, Map<AccumulatorType, FieldPair> aggregateBy, FindDTO findDTO) {
        if (groupBy == null || groupBy.isEmpty()) {
            throw new IllegalArgumentException("Missing group fields");
        }

        List<Bson> pipeline = new ArrayList<>();

        if (StringUtils.isNotEmpty(findDTO.getQuery())) {
            BsonDocument document = BsonDocument.parse(findDTO.getQuery());
            pipeline.add(Aggregates.match(document)); //Add filter
        }

        List<Bson> projections = new ArrayList<>();
        projections.add(Projections.excludeId());
        Document document = new Document();
        for (FieldPair fieldPair : groupBy) {
            document.append(fieldPair.getTargetField(), "$" + fieldPair.getOriginalField());
            projections.add(Projections.computed(fieldPair.getTargetField(), "$_id." + fieldPair.getTargetField()));
        }

        if (aggregateBy != null && !aggregateBy.isEmpty()) {
            List<BsonField> fieldAccumulators = new ArrayList<>();
            for (Map.Entry<AccumulatorType, FieldPair> entry : aggregateBy.entrySet()) {
                fieldAccumulators.add(generateAccumulator(entry.getKey(), entry.getValue()));
                projections.add(Projections.include(entry.getValue().getTargetField()));
            }
            pipeline.add(Aggregates.group(document, fieldAccumulators)); //Add aggregation
        } else {
            pipeline.add(Aggregates.group(document)); //Add aggregation
        }

        pipeline.add(Aggregates.project(Projections.fields(projections))); //Add projection

        pipeline.add(Aggregates.sort(getBsonSort(findDTO)));
        pipeline.add(Aggregates.skip(findDTO.getPage() * findDTO.getLimit()));
        pipeline.add(Aggregates.limit(findDTO.getLimit()));

        return pipeline;
    }

    private static BsonField generateAccumulator(AccumulatorType key, FieldPair value) {
        return switch (key) {
            case AccumulatorType.SUM -> Accumulators.sum(value.getTargetField(), "$" + value.getOriginalField());
            case AccumulatorType.AVG -> Accumulators.avg(value.getTargetField(), "$" + value.getOriginalField());
            case AccumulatorType.MIN -> Accumulators.min(value.getTargetField(), "$" + value.getOriginalField());
            case AccumulatorType.MAX -> Accumulators.max(value.getTargetField(), "$" + value.getOriginalField());
            case AccumulatorType.COUNT -> Accumulators.sum(value.getTargetField(), 1);
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
        Bson sort = Sorts.ascending("_id");
        if (findDTO.getOrderBy() != null && findDTO.getOrderDirection() == null)
            throw new CustomException(Response.Status.BAD_REQUEST, FindDTO.EM_MISSING_ORDER_DIRECTION);
        if (findDTO.getOrderBy() != null && findDTO.getOrderBy().length != findDTO.getOrderDirection().length)
            throw new CustomException(Response.Status.BAD_REQUEST, FindDTO.EM_INSUFFICIENT_ORDER_DIRECTION);
        if (findDTO.getOrderBy() != null && findDTO.getOrderBy().length > 0) {
            List<Bson> sorts = new ArrayList<>();
            for (int i = 0; i < findDTO.getOrderBy().length; i++) {
                sorts.add(FindDTO.ORDER_DESC.equalsIgnoreCase(findDTO.getOrderDirection()[i]) ? Sorts.descending(findDTO.getOrderBy()[i]) : Sorts.ascending(findDTO.getOrderBy()[i]));
            }
            sort = Sorts.orderBy(sorts);
        }
        return sort;
    }
}
