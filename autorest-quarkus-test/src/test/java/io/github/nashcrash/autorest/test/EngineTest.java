package io.github.nashcrash.autorest.test;

import io.github.nashcrash.autorest.test.properties.EntityTestProperties;
import io.github.nashcrash.autorest.test.properties.SubEntityTestProperties;
import io.github.nashcrash.autorest.testengine.AbstractTestEngine;
import io.github.nashcrash.autorest.testengine.EngineTestProfile;
import io.github.nashcrash.autorest.testengine.mongo.EmbeddedMongoReplicaSetTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;


@QuarkusTest
@TestProfile(EngineTestProfile.class)
@QuarkusTestResource(EmbeddedMongoReplicaSetTestResource.class)
public class EngineTest extends AbstractTestEngine {
    @Inject
    EntityTestProperties entityTestProperties;
    @Inject
    SubEntityTestProperties subEntityTestProperties;

    @BeforeEach
    void setup() {
        init(entityTestProperties, subEntityTestProperties);
    }
}
