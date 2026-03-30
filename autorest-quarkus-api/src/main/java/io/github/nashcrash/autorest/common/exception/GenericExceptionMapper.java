package io.github.nashcrash.autorest.common.exception;

import io.github.nashcrash.autorest.common.context.ContextManager;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    @Inject
    Instance<ExceptionStrategy<? extends Throwable>> strategies;

    @SuppressWarnings("unchecked")
    @Override
    public Response toResponse(Throwable e) {
        FailureMessageDTO failureMessageDTO = FailureMessageDTO.builder()
                .timestamp(Instant.now()).build();

        // Find a matching strategy for the thrown exception
        boolean handled = false;
        for (ExceptionStrategy strategy : strategies) {
            if (strategy.getExceptionClass().isInstance(e)) {
                failureMessageDTO = strategy.handle(e, failureMessageDTO);
                handled = true;
                break;
            }
        }

        // Fallback (the "else" for generic Throwable)
        if (!handled) {
            failureMessageDTO = failureMessageDTO.toBuilder()
                    .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
                    .message(e.getMessage()).build();
        }

        failureMessageDTO.setPath(ContextManager.getParameter("path"));
        log.error(failureMessageDTO.toString(), e);

        return Response.status(Response.Status.fromStatusCode(failureMessageDTO.getStatus()))
                .type(MediaType.APPLICATION_JSON)
                .entity(failureMessageDTO)
                .build();
    }
}
