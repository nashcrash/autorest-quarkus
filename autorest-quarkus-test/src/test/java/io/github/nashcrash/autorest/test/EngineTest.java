package io.github.nashcrash.autorest.test;

import io.github.nashcrash.autorest.test.properties.EntityTestProperties;
import io.github.nashcrash.autorest.test.properties.InternalEntityTestProperties;
import io.github.nashcrash.autorest.test.properties.SubEntityTestProperties;
import io.github.nashcrash.autorest.testengine.AbstractTestEngine;
import io.github.nashcrash.autorest.testengine.TestEngineProfile;
import io.github.nashcrash.autorest.testengine.TestEngineProperties;
import io.github.nashcrash.autorest.testengine.mongo.EmbeddedMongoReplicaSetTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.BeforeEach;


@QuarkusTest
@TestProfile(TestEngineProfile.class)
@QuarkusTestResource(EmbeddedMongoReplicaSetTestResource.class)
@TestEngineProperties({
        EntityTestProperties.class,
        SubEntityTestProperties.class,
        InternalEntityTestProperties.class
})
public class EngineTest extends AbstractTestEngine {

    @BeforeEach
    void setup() {
        init();
    }
}
