package io.github.nashcrash.autorest.api;

import io.github.nashcrash.autorest.common.entity.AccumulatorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE) // Active only during compilation
public @interface Aggregate {

    @interface AggregateFieldPair {
        String originalField();
        String targetField();
    }

    @interface AggregateMapEntry {
        AccumulatorType accumulator();
        AggregateFieldPair value();
    }

    AggregateFieldPair[] groupBy() default {};
    AggregateMapEntry[] aggregateBy() default {};
}
