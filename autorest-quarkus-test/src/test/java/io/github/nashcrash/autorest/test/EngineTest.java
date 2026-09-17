package io.github.nashcrash.autorest.test;

import io.github.nashcrash.autorest.testengine.AbstractTestEngine;
import io.github.nashcrash.autorest.testengine.TestEngineProfile;
import io.github.nashcrash.autorest.testengine.mongo.EmbeddedMongoReplicaSetTestResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;


@QuarkusTest
@TestProfile(TestEngineProfile.class)
@QuarkusTestResource(EmbeddedMongoReplicaSetTestResource.class)
public class EngineTest extends AbstractTestEngine {
}
