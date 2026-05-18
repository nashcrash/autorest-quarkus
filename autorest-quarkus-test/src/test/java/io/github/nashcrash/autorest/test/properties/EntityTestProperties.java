package io.github.nashcrash.autorest.test.properties;

import io.github.nashcrash.autorest.testengine.TestCasesProperties;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "entity-test")
public interface EntityTestProperties extends TestCasesProperties {
}
