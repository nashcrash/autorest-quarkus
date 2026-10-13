package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.github.nashcrash.autorest.common.entity.BusinessKeyAware;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import java.util.List;

public abstract class AbstractEntityReactiveSqlRepository<ENTITY extends AbstractEntity>
        implements PanacheRepository<ENTITY>, BusinessKeyAware<ENTITY> {

    /**
     * The current version of a historical entity, or the entity itself when it is not historical.
     * Resolves to {@code null} when nothing matches.
     */
    public Uni<ENTITY> findCurrentByBusinessKey(String businessKey) {
        return find(currentByBusinessKeyQuery(), businessKey).firstResult();
    }

    /**
     * Every version of a historical entity, oldest first; a single element otherwise.
     */
    public Uni<List<ENTITY>> findAllByBusinessKey(String businessKey) {
        return list("businessKey = ?1", Sort.by("id"), businessKey);
    }

    /**
     * Counts what must stay unique: the open versions of a historical entity, or all the rows
     * sharing the business key when the entity is not historical.
     */
    public Uni<Long> countCurrentByBusinessKey(String businessKey) {
        return count(currentByBusinessKeyQuery(), businessKey);
    }

    private String currentByBusinessKeyQuery() {
        return isHistorical()
                ? "businessKey = ?1 and endValidityDate is null"
                : "businessKey = ?1";
    }
}
