package io.github.nashcrash.autorest.api;

import io.github.nashcrash.autorest.common.entity.AbstractDTO;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Automatically generates a full CRUD REST API for the annotated entity.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE) // Active only during compilation
public @interface ResourceAPI {
    /**
     * The base path for the API (e.g., "/orders").
     */
    String basePath();

    /**
     * The Data Transfer Object (DTO) class to be used for input/output mapping.
     */
    Class<? extends AbstractDTO> dto();

    /**
     * For NOSQL database, use a hash of the specified fields, such as technical id
     */
    String[] idFields() default {};
}
