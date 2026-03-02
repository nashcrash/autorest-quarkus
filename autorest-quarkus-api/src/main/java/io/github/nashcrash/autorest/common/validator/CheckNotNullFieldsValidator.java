package io.github.nashcrash.autorest.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Objects;
import java.util.stream.Stream;

public class CheckNotNullFieldsValidator implements ConstraintValidator<CheckNotNullFields, Object> {
    private String[] properties;
    private long quantity;

    @Override
    public void initialize(CheckNotNullFields constraintAnnotation) {
        if (constraintAnnotation.value().length < 2) {
            throw new IllegalArgumentException("at least two properties needed to make a choice");
        }
        properties = constraintAnnotation.value();
        quantity = constraintAnnotation.quantity();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        try {
            BeanInfo info = Introspector.getBeanInfo(value.getClass());
            long notNull = Stream.of(properties)
                    .map(property -> Stream.of(info.getPropertyDescriptors())
                            .filter(desr -> desr.getName().equalsIgnoreCase(property))
                            .findAny()
                            .orElse(null)
                    )
                    .filter(Objects::nonNull)
                    .map(prop -> getProperty(prop, value))
                    .filter(Objects::nonNull)
                    .count();
            return notNull >= quantity;
        } catch (IntrospectionException noBean) {
            return false;
        }
    }

    private Object getProperty(PropertyDescriptor prop, Object bean) {
        try {
            return prop.getReadMethod() == null ? null : prop.getReadMethod().invoke(bean);
        } catch (ReflectiveOperationException noAccess) {
            return null;
        }
    }
}
