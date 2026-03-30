package io.github.nashcrash.autorest.common.exception.strategy;

import io.github.nashcrash.autorest.common.exception.ExceptionStrategy;
import io.github.nashcrash.autorest.common.exception.FailureMessageDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class ConstraintViolationStrategy implements ExceptionStrategy<ConstraintViolationException> {
    @Override
    public Class<ConstraintViolationException> getExceptionClass() {
        return ConstraintViolationException.class;
    }

    @Override
    public FailureMessageDTO handle(ConstraintViolationException e, FailureMessageDTO failureMessageDTO) {
        return failureMessageDTO.toBuilder().status(Response.Status.BAD_REQUEST.getStatusCode())
                .error(Response.Status.BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage()).build();
    }
}
