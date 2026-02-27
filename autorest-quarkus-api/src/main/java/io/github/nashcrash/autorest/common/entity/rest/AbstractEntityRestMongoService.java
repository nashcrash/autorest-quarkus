package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.github.nashcrash.autorest.common.util.PipelineUtils;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class AbstractEntityRestMongoService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements AbstractEntityRestService<ENTITY, DTO> {
    public static final String EM_ENTITY_NOT_FOUND_WITH_ID = "Entity not found with id: ";

    @Inject
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
        Sort sort = findDTO.getSort();
        PanacheQuery<ENTITY> entityPanacheQuery;
        if (StringUtils.isNotBlank(findDTO.getQuery())) {
            entityPanacheQuery = repository.find(findDTO.getQuery(), sort);
        } else {
            entityPanacheQuery = repository.findAll(sort);
        }
        return entityPanacheQuery.page(findDTO.getPage(), findDTO.getLimit()).list().stream().map(mapper::toDto).toList();

    }

    public DTO upsert(DTO dto) {
        ENTITY entity = mapper.toEntity(dto);
        repository.persistOrUpdate(entity);
        return mapper.toDto(entity);
    }

    public DTO patch(DTO dto) {
        ENTITY entity = repository.findById(dto.getId());
        if (entity == null)
            throw new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + dto.getId());
        mapper.patchToEntity(dto, entity);
        repository.persist(entity);
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
