package io.github.nashcrash.autorest.common.entity;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Resolves the managed entity type, so a repository can tell whether it deals with a historical
 * entity and build the right "current version" query.
 * <p>
 * A historical entity has an open version (null {@code endValidityDate}) and any number of closed
 * ones, so the current version must be filtered. A non-historical entity has a single row per
 * business key, and filtering on {@code endValidityDate} would not even compile on SQL, where the
 * column does not exist.
 */
public interface BusinessKeyAware<ENTITY extends AbstractEntity> {

    String FIELD_BUSINESS_KEY = "businessKey";
    String FIELD_END_VALIDITY_DATE = "endValidityDate";

    /**
     * Walks up the class hierarchy looking for the first parameterized superclass, which is the
     * generated repository's {@code AbstractEntity...Repository<ENTITY>}. Walking (instead of reading
     * the immediate superclass) keeps this working through the CDI client proxy, which adds a subclass.
     */
    @SuppressWarnings("unchecked")
    default Class<ENTITY> resolveEntityClass() {
        Class<?> current = getClass();
        while (current != null) {
            Type superClass = current.getGenericSuperclass();
            if (superClass instanceof ParameterizedType parameterized
                    && parameterized.getActualTypeArguments().length > 0
                    && parameterized.getActualTypeArguments()[0] instanceof Class<?> argument) {
                return (Class<ENTITY>) argument;
            }
            current = current.getSuperclass();
        }
        throw new IllegalStateException("Cannot resolve the entity type of " + getClass().getName());
    }

    default boolean isHistorical() {
        return AbstractEntityHistorical.class.isAssignableFrom(resolveEntityClass());
    }
}
