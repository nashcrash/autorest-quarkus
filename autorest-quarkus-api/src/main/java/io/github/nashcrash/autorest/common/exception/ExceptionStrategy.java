package io.github.nashcrash.autorest.common.exception;

public interface ExceptionStrategy<T extends Throwable> {
    // Defines which exception this strategy can handle
    Class<T> getExceptionClass();

    // Handles the exception and builds the DTO
    void handle(T exception, FailureMessageDTO.FailureMessageDTOBuilder builder);
}
