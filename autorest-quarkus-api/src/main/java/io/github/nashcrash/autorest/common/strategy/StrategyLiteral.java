package io.github.nashcrash.autorest.common.strategy;

import jakarta.enterprise.util.AnnotationLiteral;

public class StrategyLiteral extends AnnotationLiteral<Strategy> implements Strategy {
    private final String value;
    private final String group;

    public StrategyLiteral(String value) {
        this.value = value;
        this.group = "";
    }

    public StrategyLiteral(String value, String group) {
        this.value = value;
        this.group = group;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String group() {
        return group;
    }
}
