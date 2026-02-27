package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;

public abstract class AbstractEntityReactiveSqlRepository<ENTITY extends AbstractEntity> implements PanacheRepository<ENTITY> {
}