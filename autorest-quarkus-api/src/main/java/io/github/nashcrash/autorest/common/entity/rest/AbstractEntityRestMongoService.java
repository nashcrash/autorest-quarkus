package io.github.nashcrash.autorest.common.entity.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.AggregateIterable;
import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.github.nashcrash.autorest.common.util.PipelineUtils;
import io.github.nashcrash.autorest.common.util.SortUtils;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class AbstractEntityRestMongoService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements AbstractEntityRestService<ENTITY, DTO> {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    @Getter
    @Setter
    protected AbstractEntityRestMongoRepository<ENTITY> repository;

    @Inject
    protected AbstractEntityMapper<ENTITY, DTO> mapper;

    public DTO findById(String id) {
        ENTITY entity = repository.findById(id);
        if (entity == null)
            throw new CustomException(Response.Status.NOT_FOUND, EntityMessages.EM_ENTITY_NOT_FOUND_WITH_ID + id);
        return mapper.toDto(entity);
    }

    public DTO findByBusinessKey(String businessKey) {
        return mapper.toDto(requireCurrentByBusinessKey(businessKey));
    }

    @Transactional
    public DTO create(DTO dto) {
        if (dto.getId()!=null) {
            throw new CustomException(Response.Status.BAD_REQUEST, EntityMessages.EM_ENTITY_ID_MUST_BE_NULL);
        }
        ENTITY entity = mapper.toEntity(dto);
        requireBusinessKeyAvailable(entity.getBusinessKey());
        repository.persist(entity);
        return mapper.toDto(entity);
    }

    public List<DTO> search(FindDTO findDTO) {
        Sort sort = SortUtils.getSort(findDTO);
        PanacheQuery<ENTITY> entityPanacheQuery;
        if (StringUtils.isNotBlank(findDTO.getQuery())) {
            entityPanacheQuery = repository.find(findDTO.getQuery(), sort);
        } else {
            entityPanacheQuery = repository.findAll(sort);
        }
        return entityPanacheQuery.page(findDTO.getPage(), findDTO.getLimit()).list().stream().map(mapper::toDto).toList();

    }

    public Long count(FindDTO findDTO) {
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
    @Transactional
    public DTO upsert(DTO dto) {
        ENTITY mapped = mapper.toEntity(dto);
        String businessKey = mapped.getBusinessKey();
        requireBusinessKey(businessKey);

        ENTITY current = repository.findCurrentByBusinessKey(businessKey).orElse(null);
        if (current == null) {
            repository.persist(mapped);
            return mapper.toDto(mapped);
        }
        if (current instanceof AbstractEntityHistorical && dto instanceof AbstractHistoricalDTO historicalDTO) {
            return mapper.toDto(historize(current, dto, historicalDTO.getEndValidityDate()));
        }
        return mapper.toDto(applyInPlace(current, dto));
    }

    /**
     * Patches one specific version, identified by its technical id. A historical entity is versioned:
     * the targeted version is closed and a new open one is inserted, carrying the same business key.
     */
    @Transactional
    public DTO patch(DTO dto) {
        ENTITY entity = repository.findById(dto.getId());
        if (entity == null)
            throw new CustomException(Response.Status.NOT_FOUND, EntityMessages.EM_ENTITY_NOT_FOUND_WITH_ID + dto.getId());
        if (entity instanceof AbstractEntityHistorical historicalEntity && dto instanceof AbstractHistoricalDTO historicalDTO) {
            //If it is already historicized, there was a conflict....
            if (historicalEntity.getEndValidityDate() != null) {
                throw new CustomException(Response.Status.CONFLICT, EntityMessages.EM_ENTITY_ALREADY_HISTORIZED_WITH_ID + dto.getId());
            }
            return mapper.toDto(historize(entity, dto, historicalDTO.getEndValidityDate()));
        }
        return mapper.toDto(applyInPlace(entity, dto));
    }

    /**
     * Patches one specific version, identified by its businessKey. A historical entity is versioned:
     * the targeted version is closed and a new open one is inserted, carrying the same business key.
     */
    @Transactional
    public DTO patchByBusinessKey(String businessKey, DTO dto) {
        ENTITY entity = repository.findCurrentByBusinessKey(businessKey).orElse(null);
        if (entity == null)
            throw new CustomException(Response.Status.NOT_FOUND, EntityMessages.EM_ENTITY_NOT_FOUND_WITH_BUSINESS_KEY + businessKey);

        if (entity instanceof AbstractEntityHistorical historicalEntity && dto instanceof AbstractHistoricalDTO historicalDTO) {
            //If it is already historicized, there was a conflict....
            if (historicalEntity.getEndValidityDate() != null) {
                throw new CustomException(Response.Status.CONFLICT, EntityMessages.EM_ENTITY_ALREADY_HISTORIZED_WITH_BUSINESS_KEY + businessKey);
            }
            return mapper.toDto(historize(entity, dto, historicalDTO.getEndValidityDate()));
        }
        return mapper.toDto(applyInPlace(entity, dto));
    }

    /**
     * Updates a non-historical entity in place. The business key must survive the mapping: when it is
     * derived from business key fields, the generated mapper recomputes it, so a request that changes
     * one of those fields would silently move the entity to another identity.
     */
    private ENTITY applyInPlace(ENTITY entity, DTO dto) {
        String businessKey = entity.getBusinessKey();
        mapper.patchToEntity(dto, entity);
        mapper.addExtraEntityData(entity);
        requireBusinessKeyUnchanged(businessKey, entity.getBusinessKey());
        repository.update(entity);
        return entity;
    }

    @Transactional
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Transactional
    public void deleteByBusinessKey(String businessKey) {
        ENTITY entity = requireCurrentByBusinessKey(businessKey);
        if (entity instanceof AbstractEntityHistorical historical) {
            // Logical close: the history is kept.
            historical.setEndValidityDate(Instant.now());
            mapper.addExtraEntityData(entity);
            repository.update(entity);
        } else {
            repository.delete(entity);
        }
    }

    /**
     * Closes the current version and inserts the next one. The closed version is written first, so
     * that at no point two open versions share the same business key.
     *
     * @param closure the requested end of validity; defaults to now
     */
    private ENTITY historize(ENTITY current, DTO dto, Instant closure) {
        AbstractEntityHistorical historicalEntity = (AbstractEntityHistorical) current;
        String businessKey = current.getBusinessKey();
        Instant endValidityDate = closure != null ? closure : Instant.now();

        historicalEntity.setEndValidityDate(endValidityDate);
        mapper.addExtraEntityData(current);
        repository.update(current);

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
        requireBusinessKeyUnchanged(businessKey, newVersion.getBusinessKey());

        repository.persist(newVersion);
        return newVersion;
    }

    private ENTITY requireCurrentByBusinessKey(String businessKey) {
        requireBusinessKey(businessKey);
        return repository.findCurrentByBusinessKey(businessKey)
                .orElseThrow(() -> new CustomException(Response.Status.NOT_FOUND,
                        EntityMessages.EM_ENTITY_NOT_FOUND_WITH_BUSINESS_KEY + businessKey));
    }

    private void requireBusinessKey(String businessKey) {
        if (StringUtils.isBlank(businessKey)) {
            throw new CustomException(Response.Status.BAD_REQUEST, EntityMessages.EM_BUSINESS_KEY_REQUIRED);
        }
    }

    /**
     * Uniqueness is enforced here, inside the transaction. Note that a transaction gives atomicity,
     * not uniqueness: under true concurrency two callers can both pass this check. For a hard
     * guarantee add a unique index: on MongoDB call
     * {@code repository.ensureBusinessKeyUniqueIndex()} at startup (or create it from a script);
     * on SQL create a partial unique index in your migration, as the syntax is dialect specific.
     */
    private void requireBusinessKeyAvailable(String businessKey) {
        requireBusinessKey(businessKey);
        if (repository.countCurrentByBusinessKey(businessKey) > 0) {
            throw new CustomException(Response.Status.CONFLICT, EntityMessages.EM_BUSINESS_KEY_DUPLICATED + businessKey);
        }
    }

    private void requireBusinessKeyUnchanged(String expected, String actual) {
        if (!Objects.equals(expected, actual)) {
            throw new CustomException(Response.Status.CONFLICT, EntityMessages.EM_BUSINESS_KEY_IMMUTABLE + expected);
        }
    }

    public <T> List<T> aggregate(List<FieldPair> groupBy, List<FieldMap> aggregateBy, String elementField, String unwindFields, FindDTO findDTO, Class<T> clazz) {
        List<Bson> pipeline = PipelineUtils.aggregate(groupBy, aggregateBy, elementField, unwindFields, findDTO, false);
        return this.repository.mongoCollection().aggregate(pipeline, clazz).into(new ArrayList<>());
    }

    public <T> ResultDTO<T> aggregateAndCount(List<FieldPair> groupBy, List<FieldMap> aggregateBy, String elementField, String unwindFields, FindDTO findDTO, Class<T> clazz) {
        List<Bson> pipeline = PipelineUtils.aggregate(groupBy, aggregateBy, elementField, unwindFields, findDTO, true);
        AggregateIterable<Document> result = this.repository.mongoCollection().aggregate(pipeline, Document.class);
        Document facetResult = result.first();
        List<Document> data = facetResult.getList("data", Document.class, List.of());
        List<T> elements = data.stream().map(document -> objectMapper.convertValue(document, clazz)).toList();
        List<Document> metadataDocs = facetResult.getList("metadata", Document.class, List.of());
        long totalCount = metadataDocs.isEmpty() ? 0L : metadataDocs.getFirst().getLong("totalCount");
        return new ResultDTO<T>(elements, totalCount, findDTO.getPage(), findDTO.getLimit());
    }
}
