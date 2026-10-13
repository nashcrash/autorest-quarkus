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
     * Business fields the business key is derived from, as a hash of their values.
     * When left empty, the business key is generated once, at creation, as a random ULID.
     * It never feeds the technical id, which is always generated per version.
     */
    String[] businessKeyFields() default {};

    /**
     * Configure classes to generate.
     * The REST client is not listed here: it is generated if and only if the entity
     * is also annotated with {@link ResourceClient}.
     */
    @interface ClassesToGenerate {
        boolean resource() default true;
        boolean service() default true;
        boolean mapper() default true;
        boolean repository() default true;
    }

    ResourceAPI.ClassesToGenerate generate() default @ResourceAPI.ClassesToGenerate(resource = true, service = true, mapper = true, repository = true);
}
