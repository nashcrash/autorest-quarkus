package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.github.nashcrash.autorest.common.entity.BusinessKeyAware;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

import java.util.List;
import java.util.Optional;

public abstract class AbstractEntityRestSqlRepository<ENTITY extends AbstractEntity>
        implements PanacheRepository<ENTITY>, BusinessKeyAware<ENTITY> {

    /**
     * The current version of a historical entity, or the entity itself when it is not historical.
     */
    public Optional<ENTITY> findCurrentByBusinessKey(String businessKey) {
        return find(currentByBusinessKeyQuery(), businessKey).firstResultOptional();
    }

    /**
     * Every version of a historical entity, oldest first; a single element otherwise.
     */
    public List<ENTITY> findAllByBusinessKey(String businessKey) {
        return list("businessKey = ?1", Sort.by("id"), businessKey);
    }

    /**
     * Counts what must stay unique: the open versions of a historical entity, or all the rows
     * sharing the business key when the entity is not historical.
     */
    public long countCurrentByBusinessKey(String businessKey) {
        return count(currentByBusinessKeyQuery(), businessKey);
    }

    private String currentByBusinessKeyQuery() {
        return isHistorical()
                ? "businessKey = ?1 and endValidityDate is null"
                : "businessKey = ?1";
    }
}
