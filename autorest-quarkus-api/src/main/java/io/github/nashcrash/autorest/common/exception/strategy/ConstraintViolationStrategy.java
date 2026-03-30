package io.github.nashcrash.autorest.common.exception.strategy;

import io.github.nashcrash.autorest.common.exception.ExceptionStrategy;
import io.github.nashcrash.autorest.common.exception.FailureMessageDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.validation.ConstraintViolationException;

@ApplicationScoped
public class ConstraintViolationStrategy implements ExceptionStrategy<ConstraintViolationException> {
    @Override
    public Class<ConstraintViolationException> getExceptionClass() {
        return ConstraintViolationException.class;
    }

    @Override
    public void handle(ConstraintViolationException e, FailureMessageDTO.FailureMessageDTOBuilder builder) {
        builder.status(Response.Status.BAD_REQUEST.getStatusCode())
                .error(Response.Status.BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage());
    }
}
