package io.github.nashcrash.autorest.common.util;

import com.mongodb.client.model.*;
import io.github.nashcrash.autorest.common.entity.AccumulatorType;
import io.github.nashcrash.autorest.common.entity.FieldMap;
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

public class PipelineUtils {

    public static List<Bson> aggregate(
            List<FieldPair> groupBy,
            List<FieldMap> aggregateBy,
            String elementsKey,
            String unwindFields,
            FindDTO findDTO,
            boolean withCount
    ) {
        List<Bson> pipeline = new ArrayList<>();

        // 1) Match stage (date range or generic filter)
        if (StringUtils.isNotEmpty(findDTO.getQuery())) {
            pipeline.add(Aggregates.match(BsonDocument.parse(findDTO.getQuery())));
        }

        // 2) Sort BEFORE grouping (internal ordering, e.g. day + shift)
        if (StringUtils.isNotEmpty(elementsKey)) {
            pipeline.add(Aggregates.sort(getBsonSort(findDTO)));
        }

        if (unwindFields != null && !unwindFields.isEmpty()) {
            String fieldPath = unwindFields.startsWith("$") ? unwindFields : "$" + unwindFields;
            pipeline.add(Aggregates.unwind(fieldPath));
        }

        // 3) Group stage
        Document groupId = new Document();
        List<BsonField> accumulators = new ArrayList<>();

        if (groupBy != null && !groupBy.isEmpty()) {
            for (FieldPair fieldPair : groupBy) {
                groupId.append(
                        fieldPair.getTargetField(),
                        "$" + fieldPair.getOriginalField()
                );
            }
        }

        // Standard accumulators
        if (aggregateBy != null && !aggregateBy.isEmpty()) {
            for (FieldMap entry : aggregateBy) {
                accumulators.add(generateAccumulator(entry.getType(), entry.getOriginalField(), entry.getTargetField()));
            }
        }

        // Optional elements array: elements: { $push: "$$ROOT" }
        if (StringUtils.isNotEmpty(elementsKey)) {
            accumulators.add(
                    Accumulators.push(elementsKey, "$$ROOT")
            );
        }

        pipeline.add(Aggregates.group(groupId, accumulators));

        //Add fields from group id
        if (groupBy != null && !groupBy.isEmpty()) {
            List<Field<?>> addFields = new ArrayList<>();
            for (FieldPair fieldPair : groupBy) {
                addFields.add(new Field<>(fieldPair.getTargetField(), "$_id." + fieldPair.getOriginalField()));
            }
            pipeline.add(Aggregates.addFields(addFields));
        }

        // 4) Facet: paginated data + total count
        List<Bson> dataPipeline = new ArrayList<>();

        dataPipeline.add(Aggregates.sort(getBsonSort(findDTO)));
        dataPipeline.add(Aggregates.skip(findDTO.getPage() * findDTO.getLimit()));
        dataPipeline.add(Aggregates.limit(findDTO.getLimit()));

        // Projection: include group fields + accumulator fields
        List<Bson> projections = new ArrayList<>();
        projections.add(Projections.excludeId());

        if (groupBy != null) {
            for (FieldPair fieldPair : groupBy) {
                projections.add(
                        Projections.computed(
                                fieldPair.getTargetField(),
                                "$_id." + fieldPair.getTargetField()
                        )
                );
            }
        }

        if (aggregateBy != null) {
            for (FieldMap fieldPair : aggregateBy) {
                projections.add(Projections.include(fieldPair.getTargetField()));
            }
        }

        if (StringUtils.isNotEmpty(elementsKey)) {
            projections.add(Projections.include(elementsKey));
        }

        dataPipeline.add(
                Aggregates.project(Projections.fields(projections))
        );

        if (!withCount) {
            pipeline.add(Aggregates.sort(getBsonSort(findDTO)));
            pipeline.add(Aggregates.skip(findDTO.getPage() * findDTO.getLimit()));
            pipeline.add(Aggregates.limit(findDTO.getLimit()));
            return pipeline;
        }
        //Make final object

        List<Bson> metadataPipeline = List.of(
                Aggregates.count("totalCount")
        );

        pipeline.add(
                Aggregates.facet(
                        new Facet("data", dataPipeline),
                        new Facet("metadata", metadataPipeline)
                )
        );

        return pipeline;
    }

    private static BsonField generateAccumulator(AccumulatorType key, String originalField, String targetField) {
        String fieldReference = "$" + originalField;
        return switch (key) {
            case FIRST -> Accumulators.first(targetField, fieldReference);
            case LAST -> Accumulators.last(targetField, fieldReference);
            case SUM -> Accumulators.sum(targetField, fieldReference);
            case AVG -> Accumulators.avg(targetField, fieldReference);
            case MIN -> Accumulators.min(targetField, fieldReference);
            case MAX -> Accumulators.max(targetField, fieldReference);
            case COUNT -> Accumulators.sum(targetField, 1);
            case PROJECTION ->
                    throw new IllegalArgumentException("Accumulator type PROJECTION is used only without grouping");
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
