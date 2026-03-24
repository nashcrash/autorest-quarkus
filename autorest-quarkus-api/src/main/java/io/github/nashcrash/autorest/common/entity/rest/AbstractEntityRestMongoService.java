package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.github.nashcrash.autorest.common.util.PipelineUtils;
import io.quarkus.mongodb.panache.Panache;
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
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class AbstractEntityRestMongoService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements AbstractEntityRestService<ENTITY, DTO> {
    public static final String EM_ENTITY_NOT_FOUND_WITH_ID = "Entity not found with id: ";

    @Inject
    @Getter
    @Setter
    protected AbstractEntityRestMongoRepository<ENTITY> repository;

    @Inject
    protected AbstractEntityMapper<ENTITY, DTO> mapper;

    public DTO findById(String id) {
        ENTITY entity = repository.findById(id);
        if (entity == null) throw new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + id);
        return mapper.toDto(entity);
    }

    public DTO create(DTO dto) {
        ENTITY entity = mapper.toEntity(dto);
        repository.persist(entity);
        return mapper.toDto(entity);
    }

    public List<DTO> search(FindDTO findDTO) {
        Sort sort = PipelineUtils.getSort(findDTO);
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

    public DTO upsert(DTO dto) {
        ENTITY entity = mapper.toEntity(dto);
        repository.persistOrUpdate(entity);
        return mapper.toDto(entity);
    }

    @Transactional
    public DTO patch(DTO dto) {
        ENTITY entity = repository.findById(dto.getId());
        if (entity == null)
            throw new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + dto.getId());
        mapper.patchToEntity(dto, entity);
        if (entity instanceof AbstractEntityHistoricalMongo historicalEntity) {
            historicalEntity.setEndValidityDate(Instant.now());

            AbstractEntityHistoricalMongo newEntity = (AbstractEntityHistoricalMongo) mapper.cloneToNewInstance(entity);
            newEntity.setId(null);
            newEntity.setStartValidityDate(historicalEntity.getEndValidityDate());
            newEntity.setEndValidityDate(null);

            repository.persist((ENTITY) historicalEntity);
            repository.persist((ENTITY) newEntity);
            entity = (ENTITY) newEntity;
        } else {
            repository.persist(entity);
        }
        return mapper.toDto(entity);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
    }

    public <T> List<T> aggregate(List<FieldPair> groupBy, Map<AccumulatorType, FieldPair> aggregateBy, FindDTO findDTO, Class<T> clazz) {
        List<Bson> pipeline = PipelineUtils.aggregate(groupBy, aggregateBy, findDTO);
        return this.repository.mongoCollection().aggregate(pipeline, clazz).into(new ArrayList<>());
    }
}
