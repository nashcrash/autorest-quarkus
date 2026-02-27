package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

public abstract class AbstractEntityRestSqlRepository<ENTITY extends AbstractEntity> implements PanacheRepository<ENTITY> {
}