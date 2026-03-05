package io.github.nashcrash.autorest.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Add ResourceClient for entity
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceClient {
    /**
     * The configKey for microservice
     */
    String configKey();
}