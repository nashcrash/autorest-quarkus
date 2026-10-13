package io.github.nashcrash.autorest.testengine.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.InputStream;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MongoImportLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    /**
     * Jackson reads every YAML scalar into a String, quoted or not. A String would be stored as a BSON
     * string, which the POJO codec cannot read back into an {@code Instant} field, so any value shaped
     * exactly like an ISO-8601 instant is converted to a {@link Date} (i.e. a BSON date) on import.
     * This is what makes it possible to preload historical entities with explicit
     * startValidityDate/endValidityDate.
     */
    private static final Pattern ISO_INSTANT = Pattern.compile(
            "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d{1,9})?(Z|[+-]\\d{2}:\\d{2})");

    public static void load(MongoClient client, String resourceName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                System.out.println("Mongo import file not found: " + resourceName);
                return;
            }
            ImportSpec spec = MAPPER.readValue(is, ImportSpec.class);

            MongoDatabase db = client.getDatabase(spec.database());

            for (Map.Entry<String, List<Map<String, Object>>> entry : spec.collections().entrySet()) {
                List<Document> docs = entry.getValue().stream()
                        .map(MongoImportLoader::convertDates)
                        .map(Document::new)
                        .toList();
                if (!docs.isEmpty()) {
                    db.getCollection(entry.getKey()).insertMany(docs);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import Mongo test data", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertDates(Map<String, Object> document) {
        Map<String, Object> converted = new LinkedHashMap<>();
        document.forEach((key, value) -> converted.put(key, convertValue(value)));
        return converted;
    }

    @SuppressWarnings("unchecked")
    private static Object convertValue(Object value) {
        if (value instanceof String text) {
            return toDateOrKeep(text);
        }
        if (value instanceof Map<?, ?> map) {
            return convertDates((Map<String, Object>) map);
        }
        if (value instanceof List<?> list) {
            List<Object> convertedList = new ArrayList<>(list.size());
            list.forEach(element -> convertedList.add(convertValue(element)));
            return convertedList;
        }
        return value;
    }

    private static Object toDateOrKeep(String text) {
        if (!ISO_INSTANT.matcher(text).matches()) {
            return text;
        }
        try {
            return Date.from(Instant.parse(text));
        } catch (DateTimeParseException e) {
            try {
                // Same shape, but with an explicit offset instead of Z
                return Date.from(java.time.OffsetDateTime.parse(text).toInstant());
            } catch (DateTimeParseException ignored) {
                return text;
            }
        }
    }

    // DTO for YAML mapping
    public record ImportSpec(String database, Map<String, List<Map<String, Object>>> collections) {
    }
}
