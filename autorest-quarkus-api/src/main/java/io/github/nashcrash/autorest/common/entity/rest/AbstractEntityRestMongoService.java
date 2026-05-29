package io.github.nashcrash.autorest.common.entity.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.AggregateIterable;
import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.github.nashcrash.autorest.common.util.PipelineUtils;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
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
import java.util.Map;

@Slf4j
public abstract class AbstractEntityRestMongoService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements AbstractEntityRestService<ENTITY, DTO> {
    public static final String EM_ENTITY_ALREADY_HISTORIZED_WITH_ID = "Entity already historized with id: ";
    public static final String EM_ENTITY_NOT_FOUND_WITH_ID = "Entity not found with id: ";

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
        if (entity instanceof AbstractEntityHistoricalMongo historicalEntity && dto instanceof AbstractHistoricalDTO historicalDTO) {
            //If it is already historicized, there was a conflict....
            if (historicalEntity.getEndValidityDate() != null) {
                throw new CustomException(Response.Status.CONFLICT, EM_ENTITY_ALREADY_HISTORIZED_WITH_ID + dto.getId());
            }
            //I apply the DTO: I use the end of validity date as closure of the effect...
            if (historicalDTO.getEndValidityDate() == null) {
                historicalDTO.setEndValidityDate(Instant.now());
            }
            historicalEntity.setEndValidityDate(historicalDTO.getEndValidityDate());

            AbstractEntityHistoricalMongo newEntity = (AbstractEntityHistoricalMongo) mapper.cloneToNewInstance(entity);
            mapper.patchToEntity(dto, (ENTITY) newEntity);
            newEntity.setStartValidityDate(historicalEntity.getEndValidityDate());
            newEntity.setEndValidityDate(null);
            newEntity.setInsertionUser(null);
            newEntity.setInsertionDate(null);
            newEntity.setLastModifiedUser(null);
            newEntity.setLastModifiedDate(null);
            ///Make sure to use user in context...
            mapper.addExtraEntityData((ENTITY) newEntity);

            repository.persist((ENTITY) historicalEntity);
            repository.persist((ENTITY) newEntity);
            entity = (ENTITY) newEntity;
        } else {
            mapper.patchToEntity(dto, entity);
            repository.persist(entity);
        }
        return mapper.toDto(entity);
    }

    public void deleteById(String id) {
        repository.deleteById(id);
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
