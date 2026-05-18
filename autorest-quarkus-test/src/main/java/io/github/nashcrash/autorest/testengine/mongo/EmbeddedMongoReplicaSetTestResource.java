package io.github.nashcrash.autorest.testengine.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.ImmutableStart;
import de.flapdoodle.reverse.transitions.Start;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.bson.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class EmbeddedMongoReplicaSetTestResource
        implements QuarkusTestResourceLifecycleManager {

    private static final String REPLICA_SET_NAME = "rs0";
    //private static final int PORT = 27018;
    private static final int TIMEOUT_SECONDS_FOR_PRIMARY = 30;

    private TransitionWalker.ReachedState<RunningMongodProcess> mongod;
    private MongoClient client;
    private int port;

    @Override
    public Map<String, String> start() {
        try {
            // Allocate a free local port
            port = 27018;

            //Transaction
            ImmutableStart<Net> startTransaction = Start.to(Net.class).initializedWith(Net.of("localhost", port, false));

            ImmutableStart<MongodArguments> replSetArgs =
                    Start.to(MongodArguments.class)
                            .initializedWith(
                                    MongodArguments.builder()
                                            .replication(Storage.of(REPLICA_SET_NAME, 0))
                                            .useNoJournal(false)
                                            .build()
                            );


            // Build and start mongod with replica set enabled
            mongod = Mongod.builder()
                    .net(startTransaction)
                    .mongodArguments(replSetArgs)
                    .build()
                    .start(Version.Main.V6_0);


            // ✅ Bootstrap client (NO replicaSet here!)
            try (MongoClient bootstrap =
                         MongoClients.create("mongodb://localhost:" + port)) {

                initializeReplicaSet(bootstrap);
                waitForPrimary(bootstrap, Duration.ofSeconds(30));
            }

            String connectionString =
                    "mongodb://localhost:" + port + "/?replicaSet=" + REPLICA_SET_NAME;

            client = MongoClients.create(connectionString);

            // Load test data (import.sql equivalent)
            MongoImportLoader.load(client, "import-mongo.yaml");

            return Map.of(
                    "quarkus.mongodb.connection-string",
                    connectionString
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to start embedded MongoDB replica set (Flapdoodle 4.x)", e);
        }
    }

    @Override
    public void stop() {
        if (client != null) {
            client.close();
        }
        if (mongod != null) {
            mongod.close();
        }
    }

    private void initializeReplicaSet(MongoClient client) {
        MongoDatabase admin = client.getDatabase("admin");

        Document member = new Document()
                .append("_id", 0)
                .append("host", "localhost:" + port);

        Document config = new Document()
                .append("_id", REPLICA_SET_NAME)
                .append("members", List.of(member));

        Document command = new Document("replSetInitiate", config);

        admin.runCommand(command);
    }

    private void waitForPrimary(MongoClient client, Duration timeout) {
        MongoDatabase admin = client.getDatabase("admin");
        Instant deadline = Instant.now().plus(timeout);

        while (Instant.now().isBefore(deadline)) {
            Document status = admin.runCommand(new Document("replSetGetStatus", 1));
            for (Document member : status.getList("members", Document.class)) {
                if (member.getInteger("state") == 1) { // 1 = PRIMARY
                    return;
                }
            }
            sleep(500);
        }

        throw new IllegalStateException(
                "MongoDB replica set did not become PRIMARY within " + timeout
        );
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
