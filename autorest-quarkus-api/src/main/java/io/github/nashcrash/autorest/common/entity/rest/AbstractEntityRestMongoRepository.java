package io.github.nashcrash.autorest.common.entity.rest;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.github.nashcrash.autorest.common.entity.BusinessKeyAware;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import org.bson.Document;

import java.util.List;
import java.util.Optional;

public abstract class AbstractEntityRestMongoRepository<ENTITY extends AbstractEntity>
        implements PanacheMongoRepositoryBase<ENTITY, String>, BusinessKeyAware<ENTITY> {

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
        return list("{'businessKey': ?1}", businessKey);
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
                ? "{'businessKey': ?1, 'endValidityDate': null}"
                : "{'businessKey': ?1}";
    }

    /**
     * Creates the unique index that makes business-key uniqueness a hard guarantee, which the
     * service-side check alone cannot be under concurrency. For a historical entity the index is
     * partial, covering only the open versions, so the closed ones may share the key.
     * <p>
     * Call it once at startup (for example from a {@code StartupEvent} observer) if you want the
     * database to enforce the constraint; the library does not create it on its own.
     *
     * @return the name of the created index
     */
    public String ensureBusinessKeyUniqueIndex() {
        IndexOptions options = new IndexOptions().unique(true).name("ux_business_key");
        if (isHistorical()) {
            options.partialFilterExpression(new Document(FIELD_END_VALIDITY_DATE, new Document("$eq", null)));
        }
        return mongoCollection().createIndex(Indexes.ascending(FIELD_BUSINESS_KEY), options);
    }
}
