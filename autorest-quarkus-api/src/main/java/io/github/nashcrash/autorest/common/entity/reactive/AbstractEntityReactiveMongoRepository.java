package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;

public abstract class AbstractEntityReactiveMongoRepository<ENTITY extends AbstractEntity> implements ReactivePanacheMongoRepositoryBase<ENTITY, String> {
}