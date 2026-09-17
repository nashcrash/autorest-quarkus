package io.github.nashcrash.autorest.testengine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker for {@link TestCasesProperties} config-mapping interfaces that must be
 * automatically discovered and executed by {@link AbstractTestEngine#init()}.
 * <p>
 * Place it on interfaces that extend {@link TestCasesProperties}; the engine scans
 * the runtime classpath and registers every annotated mapping it finds.
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TestEngineProperties {
}

