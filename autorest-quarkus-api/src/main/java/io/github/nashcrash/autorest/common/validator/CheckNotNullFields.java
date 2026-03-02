package io.github.nashcrash.autorest.common.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = CheckNotNullFieldsValidator.class)
@Repeatable(CheckNotNullFieldsSet.class)
public @interface CheckNotNullFields {
    String[] value();

    long quantity() default 1;

    String message() default "insufficient not-null fields";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}