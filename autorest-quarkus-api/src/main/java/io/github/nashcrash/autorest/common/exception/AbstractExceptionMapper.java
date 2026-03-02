package io.github.nashcrash.autorest.common.exception;

import com.mongodb.MongoWriteException;
import io.github.nashcrash.autorest.common.context.ContextManager;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
    @Override
    public Response toResponse(T e) {
        FailureMessageDTO.FailureMessageDTOBuilder builder = FailureMessageDTO.builder()
                .timestamp(Instant.now());
        if (e instanceof CustomException customException) {
            builder.status(customException.getStatus().getStatusCode())
                    .error(customException.getStatus().getReasonPhrase())
                    .message(e.getMessage())
                    .extra(customException.getExtra());
        } else if (e instanceof MongoWriteException mongoWriteException) {
            Response.Status status = switch (mongoWriteException.getError().getCode()) {
                case 11000 -> Response.Status.CONFLICT;
                case 40413 -> Response.Status.EXPECTATION_FAILED;
                default -> Response.Status.BAD_REQUEST;
            };
            builder.status(status.getStatusCode())
                    .error(status.getReasonPhrase())
                    .message(mongoWriteException.getError().getMessage());
        } else if (e instanceof ConstraintViolationException constraintViolationException) {
            builder.status(Response.Status.BAD_REQUEST.getStatusCode())
                    .error(Response.Status.BAD_REQUEST.getReasonPhrase())
                    .message(e.getMessage());
        } else {
            builder.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                    .error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
                    .message(e.getMessage());
        }
        FailureMessageDTO failureMessageDTO = builder
                .path(ContextManager.getParameter("path"))
                .build();
        log.error(failureMessageDTO.toString(), e);
        return Response.status(Response.Status.fromStatusCode(failureMessageDTO.getStatus()))
                .type(MediaType.APPLICATION_JSON)
                .entity(failureMessageDTO)
                .build();
    }
}
