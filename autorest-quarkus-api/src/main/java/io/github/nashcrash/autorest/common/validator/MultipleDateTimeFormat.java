package io.github.nashcrash.autorest.common.validator;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MultipleDateTimeFormat {
    String[] patterns() default {};
    String serializePattern() default "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    String message() default "Invalid date format";
}