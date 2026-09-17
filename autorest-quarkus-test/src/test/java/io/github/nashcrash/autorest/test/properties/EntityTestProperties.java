package io.github.nashcrash.autorest.test.properties;

import io.github.nashcrash.autorest.testengine.TestCasesProperties;
import io.github.nashcrash.autorest.testengine.TestEngineProperties;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "entity-test")
@TestEngineProperties
public interface EntityTestProperties extends TestCasesProperties {
}
