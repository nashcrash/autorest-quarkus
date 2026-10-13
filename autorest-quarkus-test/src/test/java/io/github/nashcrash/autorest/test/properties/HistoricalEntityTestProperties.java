package io.github.nashcrash.autorest.test.properties;

import io.github.nashcrash.autorest.testengine.TestCasesProperties;
import io.github.nashcrash.autorest.testengine.TestEngineProperties;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "historical-entity-test")
@TestEngineProperties
public interface HistoricalEntityTestProperties extends TestCasesProperties {
}
