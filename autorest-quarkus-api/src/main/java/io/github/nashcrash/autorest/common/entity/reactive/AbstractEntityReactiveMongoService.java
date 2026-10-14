package io.github.nashcrash.autorest.common.entity.reactive;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.github.nashcrash.autorest.common.util.PipelineUtils;
import io.github.nashcrash.autorest.common.util.SortUtils;
import io.quarkus.mongodb.panache.common.reactive.Panache;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class AbstractEntityReactiveMongoService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements AbstractEntityReactiveService<ENTITY, DTO> {

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    @Getter
    @Setter
    protected AbstractEntityReactiveMongoRepository<ENTITY> repository;

    @Inject
    protected AbstractEntityMapper<ENTITY, DTO> mapper;

    public Uni<DTO> findById(String id) {
        return repository.findById(id)
                .onItem().ifNull().failWith(() -> new CustomException(Response.Status.NOT_FOUND, EntityMessages.EM_ENTITY_NOT_FOUND_WITH_ID + id))
                .onItem().transform(mapper::toDto);
    }

    public Uni<DTO> findByBusinessKey(String businessKey) {
        return requireCurrentByBusinessKey(businessKey).onItem().transform(mapper::toDto);
    }

    public Uni<DTO> create(DTO dto) {
        ENTITY entity = mapper.toEntity(dto);
        if (dto.getId()!=null) {
            return Uni.createFrom().failure(new CustomException(Response.Status.BAD_REQUEST, EntityMessages.EM_ENTITY_ID_MUST_BE_NULL));
        }
        return Panache.withTransaction(() -> requireBusinessKeyAvailable(entity.getBusinessKey())
                .chain(() -> repository.persist(entity))
                .replaceWith(() -> mapper.toDto(entity)));
    }

    public Uni<List<DTO>> search(FindDTO findDTO) {
        Sort sort = SortUtils.getSort(findDTO);
        ReactivePanacheQuery<ENTITY> eventoReactivePanacheQuery;
        if (StringUtils.isNotBlank(findDTO.getQuery())) {
            eventoReactivePanacheQuery = repository.find(findDTO.getQuery(), sort);
        } else {
            eventoReactivePanacheQuery = repository.findAll(sort);
        }
        return eventoReactivePanacheQuery.page(findDTO.getPage(), findDTO.getLimit()).list().onItem().transform(mapper::toDtos);
    }

    public Uni<Long> count(FindDTO findDTO) {
        if (StringUtils.isNotBlank(findDTO.getQuery())) {
            return repository.count(findDTO.getQuery());
        } else {
            return repository.count();
        }
    }

    /**
     * Resolves the entity by business key and applies the DTO to it: a historical entity gets a new
     * version, a non-historical one is updated in place. Creates the entity when the key is unknown.
     */
    public Uni<DTO> upsert(DTO dto) {
        ENTITY mapped = mapper.toEntity(dto);
        String businessKey = mapped.getBusinessKey();
        return Panache.withTransaction(() -> requireBusinessKey(businessKey)
                .chain(() -> repository.findCurrentByBusinessKey(businessKey))
                .onItem().transformToUni(current -> {
                    if (current == null) {
                        return repository.persist(mapped).replaceWith(() -> mapper.toDto(mapped));
                    }
                    if (current instanceof AbstractEntityHistorical && dto instanceof AbstractHistoricalDTO historicalDTO) {
                        return historize(current, dto, historicalDTO.getEndValidityDate()).onItem().transform(mapper::toDto);
                    }
                    return applyInPlace(current, dto).onItem().transform(mapper::toDto);
                }));
    }

    /**
     * Patches one specific version, identified by its technical id. A historical entity is versioned:
     * the targeted version is closed and a new open one is inserted, carrying the same business key.
     */
    public Uni<DTO> patch(DTO dto) {
        return Panache.withTransaction(() -> repository.findById(dto.getId())
                .onItem().ifNull().failWith(() -> new CustomException(Response.Status.NOT_FOUND, EntityMessages.EM_ENTITY_NOT_FOUND_WITH_ID + dto.getId()))
                .onItem().transformToUni(entity -> {
                    if (entity instanceof AbstractEntityHistorical historicalEntity && dto instanceof AbstractHistoricalDTO historicalDTO) {
                        //If it is already historicized, there was a conflict....
                        if (historicalEntity.getEndValidityDate() != null) {
                            return Uni.createFrom().failure(new CustomException(Response.Status.CONFLICT,
                                    EntityMessages.EM_ENTITY_ALREADY_HISTORIZED_WITH_ID + dto.getId()));
                        }
                        return historize(entity, dto, historicalDTO.getEndValidityDate()).onItem().transform(mapper::toDto);
                    }
                    return applyInPlace(entity, dto).onItem().transform(mapper::toDto);
                }));
    }

    public Uni<Void> deleteById(String id) {
        return repository.deleteById(id).replaceWithVoid();
    }

    public Uni<Void> deleteByBusinessKey(String businessKey) {
        return Panache.withTransaction(() -> requireCurrentByBusinessKey(businessKey)
                .onItem().transformToUni(entity -> {
                    if (entity instanceof AbstractEntityHistorical historical) {
                        // Logical close: the history is kept.
                        historical.setEndValidityDate(Instant.now());
                        mapper.addExtraEntityData(entity);
                        return repository.update(entity).replaceWithVoid();
                    }
                    return repository.delete(entity);
                }));
    }

    /**
     * Closes the current version, then inserts the next one. The two writes are chained, not combined:
     * the closed version must hit the database first, so that at no point two open versions share the
     * same business key.
     *
     * @param closure the requested end of validity; defaults to now
     */
    private Uni<ENTITY> historize(ENTITY current, DTO dto, Instant closure) {
        AbstractEntityHistorical historicalEntity = (AbstractEntityHistorical) current;
        String businessKey = current.getBusinessKey();
        Instant endValidityDate = closure != null ? closure : Instant.now();

        historicalEntity.setEndValidityDate(endValidityDate);
        mapper.addExtraEntityData(current);

        return repository.update(current).chain(() -> {
            ENTITY newVersion = mapper.cloneToNewInstance(current);
            mapper.patchToEntity(dto, newVersion);
            AbstractEntityHistorical newHistorical = (AbstractEntityHistorical) newVersion;
            newHistorical.setStartValidityDate(endValidityDate);
            newHistorical.setEndValidityDate(null);
            newVersion.setInsertionUser(null);
            newVersion.setInsertionDate(null);
            newVersion.setLastModifiedUser(null);
            newVersion.setLastModifiedDate(null);
            ///Make sure to use user in context...
            mapper.addExtraEntityData(newVersion);
            if (!Objects.equals(businessKey, newVersion.getBusinessKey())) {
                return Uni.createFrom().failure(new CustomException(Response.Status.CONFLICT,
                        EntityMessages.EM_BUSINESS_KEY_IMMUTABLE + businessKey));
            }
            return repository.persist(newVersion).replaceWith(newVersion);
        });
    }

    /**
     * Updates a non-historical entity in place. The business key must survive the mapping: when it is
     * derived from business key fields, the generated mapper recomputes it, so a request that changes
     * one of those fields would silently move the entity to another identity.
     */
    private Uni<ENTITY> applyInPlace(ENTITY entity, DTO dto) {
        String businessKey = entity.getBusinessKey();
        mapper.patchToEntity(dto, entity);
        mapper.addExtraEntityData(entity);
        if (!Objects.equals(businessKey, entity.getBusinessKey())) {
            return Uni.createFrom().failure(new CustomException(Response.Status.CONFLICT,
                    EntityMessages.EM_BUSINESS_KEY_IMMUTABLE + businessKey));
        }
        return repository.update(entity).replaceWith(entity);
    }

    private Uni<ENTITY> requireCurrentByBusinessKey(String businessKey) {
        return requireBusinessKey(businessKey)
                .chain(() -> repository.findCurrentByBusinessKey(businessKey))
                .onItem().ifNull().failWith(() -> new CustomException(Response.Status.NOT_FOUND,
                        EntityMessages.EM_ENTITY_NOT_FOUND_WITH_BUSINESS_KEY + businessKey));
    }

    private Uni<Void> requireBusinessKey(String businessKey) {
        if (StringUtils.isBlank(businessKey)) {
            return Uni.createFrom().failure(new CustomException(Response.Status.BAD_REQUEST, EntityMessages.EM_BUSINESS_KEY_REQUIRED));
        }
        return Uni.createFrom().voidItem();
    }

    /**
     * Uniqueness is enforced here, inside the transaction. Note that a transaction gives atomicity,
     * not uniqueness: under true concurrency two callers can both pass this check. For a hard
     * guarantee add a unique index: on MongoDB call
     * {@code repository.ensureBusinessKeyUniqueIndex()} at startup (or create it from a script);
     * on SQL create a partial unique index in your migration, as the syntax is dialect specific.
     */
    private Uni<Void> requireBusinessKeyAvailable(String businessKey) {
        return requireBusinessKey(businessKey)
                .chain(() -> repository.countCurrentByBusinessKey(businessKey))
                .onItem().transformToUni(count -> count > 0
                        ? Uni.createFrom().failure(new CustomException(Response.Status.CONFLICT,
                        EntityMessages.EM_BUSINESS_KEY_DUPLICATED + businessKey))
                        : Uni.createFrom().voidItem());
    }

    public <T> Uni<List<T>> aggregate(List<FieldPair> groupBy, List<FieldMap> aggregateBy, String elementField, String unwindFields, FindDTO findDTO, Class<T> clazz) {
        List<Bson> pipeline = PipelineUtils.aggregate(groupBy, aggregateBy, elementField, unwindFields, findDTO, false);
        return this.repository.mongoCollection().aggregate(pipeline, clazz).collect().asList();
    }

    public <T> Uni<ResultDTO<T>> aggregateAndCount(List<FieldPair> groupBy, List<FieldMap> aggregateBy, String elementField, String unwindFields, FindDTO findDTO, Class<T> clazz) {
        List<Bson> pipeline = PipelineUtils.aggregate(groupBy, aggregateBy, elementField, unwindFields, findDTO, true);
        return this.repository.mongoCollection().aggregate(pipeline, Document.class).toUni().map(facetResult -> {
            List<Document> data = facetResult.getList("data", Document.class, List.of());
            List<T> elements = data.stream().map(document -> objectMapper.convertValue(document, clazz)).toList();
            List<Document> metadataDocs = facetResult.getList("metadata", Document.class, List.of());
            long totalCount = ((Number) (metadataDocs.isEmpty() ? 0L : metadataDocs.getFirst().get("totalCount"))).longValue();
            return new ResultDTO<>(elements, totalCount, findDTO.getPage(), findDTO.getLimit());
        });
    }
}
