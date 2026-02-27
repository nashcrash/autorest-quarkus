package io.github.nashcrash.autorest.common.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper extends AbstractExceptionMapper<ConstraintViolationException> {
}
