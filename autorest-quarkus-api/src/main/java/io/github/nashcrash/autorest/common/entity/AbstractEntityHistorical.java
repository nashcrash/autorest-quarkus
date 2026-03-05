package io.github.nashcrash.autorest.common.entity;

import java.time.Instant;

public interface AbstractEntityHistorical {
    Instant getStartValidityDate();
    void setStartValidityDate(Instant startValidityDate);
    Instant getEndValidityDate();
    void setEndValidityDate(Instant endValidityDate);
}
