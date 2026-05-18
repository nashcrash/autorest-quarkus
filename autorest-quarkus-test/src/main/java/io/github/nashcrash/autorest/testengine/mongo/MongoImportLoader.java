package io.github.nashcrash.autorest.testengine.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class MongoImportLoader {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    public static void load(MongoClient client, String resourceName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                System.out.println("Mongo import file not found: " + resourceName);
                return;
            }
            ImportSpec spec = MAPPER.readValue(is, ImportSpec.class);

            MongoDatabase db = client.getDatabase(spec.database());

            for (Map.Entry<String, List<Map<String, Object>>> entry : spec.collections().entrySet()) {
                List<Document> docs = entry.getValue().stream().map(Document::new).toList();
                if (!docs.isEmpty()) {
                    db.getCollection(entry.getKey()).insertMany(docs);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to import Mongo test data", e);
        }
    }

    // DTO for YAML mapping
    public record ImportSpec(String database, Map<String, List<Map<String, Object>>> collections) {
    }
}
