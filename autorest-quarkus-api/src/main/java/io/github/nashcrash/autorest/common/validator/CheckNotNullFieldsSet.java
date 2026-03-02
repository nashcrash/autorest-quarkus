package io.github.nashcrash.autorest.common.validator;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CheckNotNullFieldsSet {
    CheckNotNullFields[] value();
}
