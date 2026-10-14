package io.github.nashcrash.autorest.common.entity;

/**
 * Error messages shared by the generated service layer.
 * They used to be duplicated as constants in each of the four service implementations.
 */
public final class EntityMessages {

    public static final String EM_ENTITY_NOT_FOUND_WITH_ID = "Entity not found with id: ";
    public static final String EM_ENTITY_NOT_FOUND_WITH_BUSINESS_KEY = "Entity not found with business key: ";
    public static final String EM_ENTITY_ALREADY_HISTORIZED_WITH_ID = "Entity already historized with id: ";
    public static final String EM_BUSINESS_KEY_DUPLICATED = "Business key already in use: ";
    public static final String EM_BUSINESS_KEY_IMMUTABLE = "Business key cannot be modified, it is derived from the declared business key fields: ";
    public static final String EM_BUSINESS_KEY_REQUIRED = "Business key is required";
    public static final String EM_INVALID_ID = "Invalid id: ";
    public static final String EM_AGGREGATE_NOT_SUPPORTED = "Aggregations are supported on MongoDB entities only";
    public static final String EM_ENTITY_ID_MUST_BE_NULL = "Entity id must be null";

    private EntityMessages() {
    }
}
