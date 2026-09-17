package io.github.nashcrash.autorest.test.properties;

import io.github.nashcrash.autorest.testengine.TestCasesProperties;
import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "internal-entity-test")
public interface InternalEntityTestProperties extends TestCasesProperties {
}
