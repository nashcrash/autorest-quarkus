package io.github.nashcrash.autorest.common.util;

import io.github.nashcrash.autorest.common.entity.AccumulatorType;
import io.github.nashcrash.autorest.common.entity.FieldMap;
import io.github.nashcrash.autorest.common.entity.FieldPair;
import io.github.nashcrash.autorest.common.entity.FindDTO;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class PipelineUtilsTest {
    @Test
    public void test_Aggregate() {
        String expectedResult="[{\"$group\": {\"_id\": {\"day\": \"$day\"}, \"highThreshold\": {\"$max\": \"$highThreshold\"}, \"lowThreshold\": {\"$max\": \"$lowThreshold\"}, \"disturbedState\": {\"$max\": \"$disturbedState\"}, \"totalDailyEvents\": {\"$max\": \"$totalDailyEvents\"}, \"timeWindow\": {\"$max\": \"$timeWindow\"}, \"timeZone\": {\"$max\": \"$timeZone\"}}}, {\"$addFields\": {\"day\": \"$_id.day\"}}, {\"$sort\": {\"day\": -1, \"shift\": -1}}, {\"$skip\": 0}, {\"$limit\": 100}]";
        List<Bson> aggregate = PipelineUtils.aggregate(
                List.of(FieldPair.of("day", "day")),
                List.of(
                        FieldMap.of(AccumulatorType.MAX, "highThreshold", "highThreshold"),
                        FieldMap.of(AccumulatorType.MAX, "lowThreshold", "lowThreshold"),
                        FieldMap.of(AccumulatorType.MAX, "disturbedState", "disturbedState"),
                        FieldMap.of(AccumulatorType.MAX, "totalDailyEvents", "totalDailyEvents"),
                        FieldMap.of(AccumulatorType.MAX, "timeWindow", "timeWindow"),
                        FieldMap.of(AccumulatorType.MAX, "timeZone", "timeZone")
                ),
                null,
                null,
                FindDTO.builder()
                        .orderBy(new String[] {"day", "shift"})
                        .orderDirection(new String[] {FindDTO.ORDER_DESC, FindDTO.ORDER_DESC})
                        .build(),
                false);

        Assertions.assertEquals(expectedResult, toJson(aggregate));
    }

    @Test
    public void test_AggregateAndCount() {
        String expectedResult="[{\"$group\": {\"_id\": {\"day\": \"$day\"}, \"highThreshold\": {\"$max\": \"$highThreshold\"}, \"lowThreshold\": {\"$max\": \"$lowThreshold\"}, \"disturbedState\": {\"$max\": \"$disturbedState\"}, \"totalDailyEvents\": {\"$max\": \"$totalDailyEvents\"}, \"timeWindow\": {\"$max\": \"$timeWindow\"}, \"timeZone\": {\"$max\": \"$timeZone\"}}}, {\"$addFields\": {\"day\": \"$_id.day\"}}, {\"$facet\": {\"data\": [{\"$sort\": {\"day\": -1, \"shift\": -1}}, {\"$skip\": 0}, {\"$limit\": 100}, {\"$project\": {\"_id\": 0, \"day\": \"$_id.day\", \"highThreshold\": 1, \"lowThreshold\": 1, \"disturbedState\": 1, \"totalDailyEvents\": 1, \"timeWindow\": 1, \"timeZone\": 1}}], \"metadata\": [{\"$count\": \"totalCount\"}]}}]";
        List<Bson> aggregate = PipelineUtils.aggregate(
                List.of(FieldPair.of("day", "day")),
                List.of(
                        FieldMap.of(AccumulatorType.MAX, "highThreshold", "highThreshold"),
                        FieldMap.of(AccumulatorType.MAX, "lowThreshold", "lowThreshold"),
                        FieldMap.of(AccumulatorType.MAX, "disturbedState", "disturbedState"),
                        FieldMap.of(AccumulatorType.MAX, "totalDailyEvents", "totalDailyEvents"),
                        FieldMap.of(AccumulatorType.MAX, "timeWindow", "timeWindow"),
                        FieldMap.of(AccumulatorType.MAX, "timeZone", "timeZone")
                ),
                null,
                null,
                FindDTO.builder()
                        .orderBy(new String[] {"day", "shift"})
                        .orderDirection(new String[] {FindDTO.ORDER_DESC, FindDTO.ORDER_DESC})
                        .build(),
                true);

        Assertions.assertEquals(expectedResult, toJson(aggregate));
    }

    @Test
    public void test_Aggregate_withElementKey() {
        String expectedResult="[{\"$sort\": {\"day\": -1, \"shift\": -1}}, {\"$group\": {\"_id\": {\"day\": \"$day\"}, \"highThreshold\": {\"$max\": \"$highThreshold\"}, \"lowThreshold\": {\"$max\": \"$lowThreshold\"}, \"disturbedState\": {\"$max\": \"$disturbedState\"}, \"totalDailyEvents\": {\"$max\": \"$totalDailyEvents\"}, \"timeWindow\": {\"$max\": \"$timeWindow\"}, \"timeZone\": {\"$max\": \"$timeZone\"}, \"intervals\": {\"$push\": \"$$ROOT\"}}}, {\"$addFields\": {\"day\": \"$_id.day\"}}, {\"$sort\": {\"day\": -1, \"shift\": -1}}, {\"$skip\": 0}, {\"$limit\": 100}]";
        List<Bson> aggregate = PipelineUtils.aggregate(
                List.of(FieldPair.of("day", "day")),
                List.of(
                        FieldMap.of(AccumulatorType.MAX, "highThreshold", "highThreshold"),
                        FieldMap.of(AccumulatorType.MAX, "lowThreshold", "lowThreshold"),
                        FieldMap.of(AccumulatorType.MAX, "disturbedState", "disturbedState"),
                        FieldMap.of(AccumulatorType.MAX, "totalDailyEvents", "totalDailyEvents"),
                        FieldMap.of(AccumulatorType.MAX, "timeWindow", "timeWindow"),
                        FieldMap.of(AccumulatorType.MAX, "timeZone", "timeZone")
                ),
                "intervals",
                (String) null,
                FindDTO.builder()
                        .orderBy(new String[] {"day", "shift"})
                        .orderDirection(new String[] {FindDTO.ORDER_DESC, FindDTO.ORDER_DESC})
                        .build(),
                false);

        Assertions.assertEquals(expectedResult, toJson(aggregate));
    }

    @Test
    public void test_AggregateAndCount_withElementKey() {
        String expectedResult="[{\"$sort\": {\"day\": -1, \"shift\": -1}}, {\"$group\": {\"_id\": {\"day\": \"$day\"}, \"highThreshold\": {\"$max\": \"$highThreshold\"}, \"lowThreshold\": {\"$max\": \"$lowThreshold\"}, \"disturbedState\": {\"$max\": \"$disturbedState\"}, \"totalDailyEvents\": {\"$max\": \"$totalDailyEvents\"}, \"timeWindow\": {\"$max\": \"$timeWindow\"}, \"timeZone\": {\"$max\": \"$timeZone\"}, \"intervals\": {\"$push\": \"$$ROOT\"}}}, {\"$addFields\": {\"day\": \"$_id.day\"}}, {\"$facet\": {\"data\": [{\"$sort\": {\"day\": -1, \"shift\": -1}}, {\"$skip\": 0}, {\"$limit\": 100}, {\"$project\": {\"_id\": 0, \"day\": \"$_id.day\", \"highThreshold\": 1, \"lowThreshold\": 1, \"disturbedState\": 1, \"totalDailyEvents\": 1, \"timeWindow\": 1, \"timeZone\": 1, \"intervals\": 1}}], \"metadata\": [{\"$count\": \"totalCount\"}]}}]";
        List<Bson> aggregate = PipelineUtils.aggregate(
                List.of(FieldPair.of("day", "day")),
                List.of(
                        FieldMap.of(AccumulatorType.MAX, "highThreshold", "highThreshold"),
                        FieldMap.of(AccumulatorType.MAX, "lowThreshold", "lowThreshold"),
                        FieldMap.of(AccumulatorType.MAX, "disturbedState", "disturbedState"),
                        FieldMap.of(AccumulatorType.MAX, "totalDailyEvents", "totalDailyEvents"),
                        FieldMap.of(AccumulatorType.MAX, "timeWindow", "timeWindow"),
                        FieldMap.of(AccumulatorType.MAX, "timeZone", "timeZone")
                ),
                "intervals",
                (String) null,
                FindDTO.builder()
                        .orderBy(new String[] {"day", "shift"})
                        .orderDirection(new String[] {FindDTO.ORDER_DESC, FindDTO.ORDER_DESC})
                        .build(),
                true);

        Assertions.assertEquals(expectedResult, toJson(aggregate));
    }

    private String toJson(List<Bson> aggregate) {
        return "[" +
                aggregate.stream()
                        .map(stage -> stage.toBsonDocument().toJson())
                        .collect(Collectors.joining(", "))
                + "]";
    }
}
