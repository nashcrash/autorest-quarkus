package io.github.nashcrash.autorest.common.exception.strategy;

import io.github.nashcrash.autorest.common.exception.ExceptionStrategy;
import io.github.nashcrash.autorest.common.exception.FailureMessageDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import com.mongodb.MongoWriteException;
import io.quarkus.arc.properties.UnlessBuildProperty;

@ApplicationScoped
// This bean will ONLY be registered if 'quarkus.mongodb.enabled' is NOT set to false
@UnlessBuildProperty(name = "quarkus.mongodb.enabled", stringValue = "false")
public class MongoWriteExceptionStrategy implements ExceptionStrategy<MongoWriteException> {

    @Override
    public Class<MongoWriteException> getExceptionClass() {
        return MongoWriteException.class;
    }

    @Override
    public void handle(MongoWriteException e, FailureMessageDTO.FailureMessageDTOBuilder builder) {
        Response.Status status = switch (e.getError().getCode()) {
            case 11000 -> Response.Status.CONFLICT;
            case 40413 -> Response.Status.EXPECTATION_FAILED;
            default -> Response.Status.BAD_REQUEST;
        };

        builder.status(status.getStatusCode())
                .error(status.getReasonPhrase())
                .message(e.getError().getMessage());
    }
}