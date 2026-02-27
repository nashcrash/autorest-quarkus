package io.github.nashcrash.autorest.common.exception;

import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class CustomException extends RuntimeException {
    @Builder.Default
    private Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
    @Builder.Default
    private String message = Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase();
    private Map<String, Object> extra;

    public CustomException() {
        super();
        this.extra = new HashMap<>();
    }

    public CustomException(Throwable cause) {
        super(cause);
        this.extra = new HashMap<>();
    }

    public CustomException(Response.Status status) {
        this();
        this.status = status;
        this.message = status.getReasonPhrase();
    }

    public CustomException(Response.Status status, String message) {
        this();
        this.status = status;
        this.message = message;
    }

    public CustomException(Response.Status status, Throwable cause) {
        this(cause);
        this.status = status;
        this.message = status.getReasonPhrase();
    }

    public CustomException(Response.Status status, String message, Throwable cause) {
        this(cause);
        this.status = status;
        this.message = message;
    }
}

