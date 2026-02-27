package io.github.nashcrash.autorest.common.entity.messaging;

import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.DeserializationFailureHandler;
import org.apache.kafka.common.header.Headers;

import java.time.Duration;
import java.util.Arrays;

public abstract class AbstractEntityDeserializationErrorHandler<DTO extends AbstractEntity> implements DeserializationFailureHandler<DTO> {

    @Override
    public DTO decorateDeserialization(
            Uni<DTO> deserialization,
            String topic,
            boolean isKey,
            String deserializer,
            byte[] data,
            Headers headers) {


        return deserialization
                .onFailure()
                .recoverWithItem(
                        error -> {
                            Log.error("Error while deserializing the object: " + Arrays.toString(data), error);
                            return null;
                        })
                .await().atMost(Duration.ofMillis(50));
    }
}
