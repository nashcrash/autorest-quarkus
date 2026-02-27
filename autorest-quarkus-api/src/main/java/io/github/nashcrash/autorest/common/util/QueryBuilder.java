package io.github.nashcrash.autorest.common.util;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class QueryBuilder {
    private static final Map<String, String> templateSQL = new HashMap<>();
    private static final Map<String, String> templateMongo = new HashMap<>();

    static {
        templateSQL.put("objectid", "'#value#'");
        templateSQL.put("isodate", "'#value#'");
        templateSQL.put("escape", "'");
        templateSQL.put("and", " and ");
        templateSQL.put("eq", "#field# = #value#");
        templateSQL.put("ne", "#field# <> #value#");
        templateSQL.put("in", "#field# in (#value#)");
        templateSQL.put("notin", "#field# not in (#value#)");
        templateSQL.put("lt", "#field# < #value#");
        templateSQL.put("gt", "#field# > #value#");
        templateSQL.put("lte", "#field# <= #value#");
        templateSQL.put("gte", "#field# >= #value#");
        templateSQL.put("isnull", "#field# is NULL");
        templateSQL.put("isnotnull", "#field# is NOT NULL");
        templateSQL.put("between", "#field# between #value# and #value2#");
        templateSQL.put("notbetween", "#field# not between #value# and #value2#");
        templateMongo.put("objectid", "ObjectId(\"#value#\")");
        templateMongo.put("isodate", "ISODate(\"#value#\")");
        templateMongo.put("escape", "\"");
        templateMongo.put("and", ",");
        templateMongo.put("eq", "#field#: #value#");
        templateMongo.put("ne", "#field#: {$ne: #value#}");
        templateMongo.put("in", "#field#: {$in: [#value#]}");
        templateMongo.put("notin", "#field#: {$not: {$in: [#value#]}}");
        templateMongo.put("lt", "#field#: {$lt: #value#}");
        templateMongo.put("gt", "#field#: {$gt: #value#}");
        templateMongo.put("lte", "#field#: {$lte: #value#}");
        templateMongo.put("gte", "#field#: {$gte: #value#}");
        templateMongo.put("isnull", "#field#: null");
        templateMongo.put("isnotnull", "#field#: {$ne: null}");
        templateMongo.put("between", "#field#: {$gte: #value#, $lte: #value2#}");
        templateMongo.put("notbetween", "#field#: {$not: {$gte: #value#, $lte: #value2#}}");
    }

    private String type;
    private List<Condition> conditions;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

    public static QueryBuilder getMongoQueryBuilder() {
        return new QueryBuilder("mongo");
    }

    public static QueryBuilder getSQLQueryBuilder() {
        return new QueryBuilder("sql");
    }

    private QueryBuilder(String type) {
        this.type = type;
        this.conditions = new ArrayList<>();
    }

    public QueryBuilder withDateFormatter(DateTimeFormatter dateFormatter) {
        this.dateFormatter = dateFormatter;
        return this;
    }

    public QueryBuilder withDateTimeFormatter(DateTimeFormatter dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
        return this;
    }

    public <T> QueryBuilder andEq(String field, T value) {
        conditions.add(new Condition(field, "eq", value));
        return this;
    }

    public <T> QueryBuilder andNe(String field, T value) {
        conditions.add(new Condition(field, "ne", value));
        return this;
    }

    public <T> QueryBuilder andIn(String field, List<T> value) {
        conditions.add(new Condition(field, "in", value));
        return this;
    }

    public <T> QueryBuilder andNotIn(String field, List<T> value) {
        conditions.add(new Condition(field, "notin", value));
        return this;
    }

    public <T> QueryBuilder andLt(String field, T value) {
        conditions.add(new Condition(field, "lt", value));
        return this;
    }

    public <T> QueryBuilder andGt(String field, T value) {
        conditions.add(new Condition(field, "gt", value));
        return this;
    }

    public <T> QueryBuilder andLte(String field, T value) {
        conditions.add(new Condition(field, "lte", value));
        return this;
    }

    public <T> QueryBuilder andGte(String field, T value) {
        conditions.add(new Condition(field, "gte", value));
        return this;
    }

    public <T> QueryBuilder andIsNull(String field) {
        conditions.add(new Condition(field, "isnull", null));
        return this;
    }

    public <T> QueryBuilder andIsNotNull(String field) {
        conditions.add(new Condition(field, "isnotnull", null));
        return this;
    }

    public <T> QueryBuilder andAutoBetween(String field, T valueFrom, T valueTo) {
        String type = "between";
        if (valueFrom != null && valueTo == null) {
            type = "gte";
        }
        if (valueFrom == null && valueTo != null) {
            type = "lte";
            valueFrom = valueTo;
        }
        conditions.add(new Condition(field, type, valueFrom, valueTo));
        return this;
    }

    public <T> QueryBuilder andBetween(String field, T valueFrom, T valueTo) {
        conditions.add(new Condition(field, "between", valueFrom, valueTo));
        return this;
    }

    public <T> QueryBuilder andNotBetween(String field, T valueFrom, T valueTo) {
        conditions.add(new Condition(field, "notbetween", valueFrom, valueTo));
        return this;
    }

    public String build() {
        if ("mongo".equalsIgnoreCase(type)) {
            return "{" + build(templateMongo) + "}";
        }
        return build(templateSQL);
    }

    private String build(Map<String, String> template) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (Condition condition : conditions) {
            if (first) {
                first = false;
            } else {
                stringBuilder.append(template.get("and"));
            }
            stringBuilder.append(
                    template.get(condition.type)
                            .replaceAll("#field#", condition.field)
                            .replaceAll("#value#", escape(condition.value, template))
                            .replaceAll("#value2#", escape(condition.value2, template))
            );
        }
        return stringBuilder.toString();
    }

    private String escape(Object value, final Map<String, String> template) {
        final String escape = template.get("escape");
        if (value instanceof String) {
            return escape + value + escape;
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof ObjectId objectId) {
            String isodate = template.get("objectid");
            return isodate.replaceAll("#value#", objectId.toHexString());
        } else if (value instanceof LocalDate localDate) {
            String isodate = template.get("isodate");
            return isodate.replaceAll("#value#", localDate.format(this.dateFormatter));
        } else if (value instanceof Instant instant) {
            String isodate = template.get("isodate");
            return isodate.replaceAll("#value#", this.dateTimeFormatter.withZone(ZoneId.systemDefault()).format(instant));
        } else if (value instanceof Date date) {
            String isodate = template.get("isodate");
            return isodate.replaceAll("#value#", this.dateTimeFormatter.withZone(ZoneId.systemDefault()).format(date.toInstant()));
        } else if (value instanceof Boolean) {
            return String.valueOf(value);
        } else if (value instanceof Collection<?> collection) {
            List<?> result = collection.stream().map(e -> escape(e, template)).collect(Collectors.toList());
            return StringUtils.join(result.toArray(), ",");
        } else if (value == null) {
            return "null";
        }
        throw new IllegalArgumentException("Conversion error: [" + value.getClass().getSimpleName() + "]: " + value);
    }

    private String escapeList(List<String> codiceTipoFattura, final String escape) {
        return StringUtils.join(codiceTipoFattura.stream().map(e -> escape + e + escape).collect(Collectors.toList()), ",");
    }

    private String dataRange(String fieldName, String dataDa, String dataA) {
        StringBuilder query = new StringBuilder();
        query.append(", ").append(fieldName).append(": {");
        if (dataDa != null) {
            query.append("$gte: ISODate(\"").append(dataDa).append("\")");
        }
        if (dataA != null) {
            if (dataDa != null) {
                query.append(",");
            }
            query.append("$lte: ISODate(\"").append(dataA).append("\")");
        }
        query.append("}");
        return query.toString();
    }
}

class Condition {
    String field;
    String type;
    Object value;
    Object value2;

    public Condition(String field, String type, Object value) {
        this.field = field;
        this.type = type;
        this.value = value;
    }

    public Condition(String field, String type, Object value, Object value2) {
        this.field = field;
        this.type = type;
        this.value = value;
        this.value2 = value2;
    }
}

