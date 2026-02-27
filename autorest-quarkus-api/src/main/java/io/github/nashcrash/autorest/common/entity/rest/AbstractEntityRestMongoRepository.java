package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;

public abstract class AbstractEntityRestMongoRepository<ENTITY extends AbstractEntity> implements PanacheMongoRepositoryBase<ENTITY, String> {
}