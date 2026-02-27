package io.github.nashcrash.autorest.common.util;

import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Projections;
import io.github.nashcrash.autorest.common.entity.AccumulatorType;
import io.github.nashcrash.autorest.common.entity.FieldPair;
import io.github.nashcrash.autorest.common.entity.FindDTO;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PipelineUtils {

    //TODO add Sort, groupBy, limit, page
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
}
