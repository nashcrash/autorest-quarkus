package io.github.nashcrash.autorest.common.exception;

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
        FailureMessageDTO failureMessageDTO = null;
        if (e instanceof CustomException) {
            failureMessageDTO = generateFailureMessageDTO((CustomException) e);
        } else if (e instanceof ConstraintViolationException) {
            failureMessageDTO = generateFailureMessageDTO((ConstraintViolationException) e);
        } else {
            failureMessageDTO = generateFailureMessageDTO(e);
        }
        log.error(failureMessageDTO.toString(), e);
        return Response.status(Response.Status.fromStatusCode(failureMessageDTO.getStatus()))
                .type(MediaType.APPLICATION_JSON)
                .entity(failureMessageDTO)
                .build();
    }

    protected FailureMessageDTO generateFailureMessageDTO(ConstraintViolationException e) {
        return FailureMessageDTO.builder()
                .timestamp(Instant.now())
                .status(Response.Status.BAD_REQUEST.getStatusCode())
                .error(Response.Status.BAD_REQUEST.getReasonPhrase())
                .message(e.getMessage())
                .path(ContextManager.getParameter("path"))
                .build();
    }

    protected FailureMessageDTO generateFailureMessageDTO(Throwable e) {
        return FailureMessageDTO.builder()
                .timestamp(Instant.now())
                .status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .error(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message(e.getMessage())
                .path(ContextManager.getParameter("path"))
                .build();
    }

    protected FailureMessageDTO generateFailureMessageDTO(CustomException e) {
        return FailureMessageDTO.builder()
                .timestamp(Instant.now())
                .status(e.getStatus().getStatusCode())
                .error(e.getStatus().getReasonPhrase())
                .message(e.getMessage())
                .extra(e.getExtra())
                .path(ContextManager.getParameter("path"))
                .build();
    }
}
