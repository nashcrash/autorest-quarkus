package io.github.nashcrash.autorest.common.context;

import lombok.Getter;

public enum ContextHeader {
    ACCEPT("Accept"),
    AUTHORIZATION("Authorization"),
    X_USERNAME("x-custom-username"),
    X_TRANSACTION_ID("x-custom-transactionId");

    @Getter
    String value;

    ContextHeader(String value) {
        this.value = value;
    }

    public static ContextHeader fromString(String value) {
        for (ContextHeader c : values()) {
            if (c.getValue().equalsIgnoreCase(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Invalid ContextHeader: " + value);
    }
}
