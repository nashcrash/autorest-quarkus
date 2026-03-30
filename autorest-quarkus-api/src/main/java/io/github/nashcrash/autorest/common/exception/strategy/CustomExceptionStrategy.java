package io.github.nashcrash.autorest.common.exception.strategy;

import io.github.nashcrash.autorest.common.exception.CustomException;
import io.github.nashcrash.autorest.common.exception.ExceptionStrategy;
import io.github.nashcrash.autorest.common.exception.FailureMessageDTO;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CustomExceptionStrategy implements ExceptionStrategy<CustomException> {
    @Override
    public Class<CustomException> getExceptionClass() {
        return CustomException.class;
    }

    @Override
    public void handle(CustomException e, FailureMessageDTO.FailureMessageDTOBuilder builder) {
        builder.status(e.getStatus().getStatusCode())
                .error(e.getStatus().getReasonPhrase())
                .message(e.getMessage())
                .extra(e.getExtra());
    }
}
