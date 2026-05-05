package io.github.nashcrash.autorest.api;

import io.github.nashcrash.autorest.common.entity.AccumulatorType;

import java.lang.annotation.*;

@Repeatable(Aggregates.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE) // Active only during compilation
public @interface Aggregate {
    String name();

    String path();

    /**
     * The Data Transfer Object (DTO) class to be used for input/output mapping.
     */
    Class<?> dto();

    @interface AggregateFieldPair {
        String originalField();

        String targetField();
    }

    @interface AggregateUnwindField {
        String originalField();
    }

    @interface AggregateMapEntry {
        AccumulatorType accumulator();

        AggregateFieldPair value();
    }

    AggregateFieldPair[] groupBy() default {};
    AggregateMapEntry[] aggregateBy() default {};
    AggregateUnwindField unwind() default @AggregateUnwindField(originalField = "");
}
