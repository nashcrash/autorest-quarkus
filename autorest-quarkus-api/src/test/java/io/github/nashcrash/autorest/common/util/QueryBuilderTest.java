package io.github.nashcrash.autorest.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QueryBuilderTest {

    @Test
    public void test_Mongo_HistoricalReference() {
        Date referenceDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse("2026-03-09T13:12:45.480+01:00", new ParsePosition(0));
        String expected = "{code: \"PIPPO\",description: {$regex: \".*aaa.*\"},start: {$lte: ISODate(\"2026-03-09T13:12:45.480+01:00\")},\"$or\": [ {end: {$gt: ISODate(\"2026-03-09T13:12:45.480+01:00\")}},{end: null} ]}";
        String query = QueryBuilder
                .getMongoQueryBuilder()
                .andEq("code", "PIPPO")
                .andMatch("description", ".*aaa.*")
                .andHistoricalReference("start", "end", referenceDate)
                .build();
        Assertions.assertEquals(expected, query);
    }

    @Test
    public void test_Mongo_HistoricalReference2() {
        Date referenceDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse("2026-03-09T13:12:45.480+01:00", new ParsePosition(0));
        String expected = "{code: \"PIPPO\",description: {$regex: \"\\.\\*aaa\\.\\*\"},start: {$lte: ISODate(\"2026-03-09T13:12:45.480+01:00\")},\"$or\": [ {end: {$gt: ISODate(\"2026-03-09T13:12:45.480+01:00\")}},{end: null} ]}";
        String query = QueryBuilder
                .getMongoQueryBuilder()
                .andEq("code", "PIPPO")
                .andMatch("description", QueryBuilder.cleanMatchString(".*aaa.*"))
                .andHistoricalReference("start", "end", referenceDate)
                .build();
        Assertions.assertEquals(expected, query);
    }

    @Test
    public void test_SQL_HistoricalReference() {
        Date referenceDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse("2026-03-09T13:12:45.480+01:00", new ParsePosition(0));
        String expected = "code = 'PIPPO' and description not like '%aaa%' ESCAPE '\\' and start <= '2026-03-09T13:12:45.480+01:00' and ( end > '2026-03-09T13:12:45.480+01:00' or end is NULL )";
        String query = QueryBuilder
                .getSQLQueryBuilder()
                .andEq("code", "PIPPO")
                .andNotMatch("description", "%aaa%")
                .andHistoricalReference("start", "end", referenceDate)
                .build();
        Assertions.assertEquals(expected, query);
    }

    @Test
    public void test_SQL_HistoricalReference2() {
        Date referenceDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse("2026-03-09T13:12:45.480+01:00", new ParsePosition(0));
        String expected = "code = 'PIPPO' and description not like '\\%aaa\\%' ESCAPE '\\' and start <= '2026-03-09T13:12:45.480+01:00' and ( end > '2026-03-09T13:12:45.480+01:00' or end is NULL )";
        String query = QueryBuilder
                .getSQLQueryBuilder()
                .andEq("code", "PIPPO")
                .andNotMatch("description", QueryBuilder.cleanMatchString("%aaa%"))
                .andHistoricalReference("start", "end", referenceDate)
                .build();
        Assertions.assertEquals(expected, query);
    }
}
