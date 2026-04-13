package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.github.nashcrash.autorest.common.util.PipelineUtils;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
public class AbstractEntityRestSqlService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements AbstractEntityRestService<ENTITY, DTO> {
    public static final String EM_ENTITY_ALREADY_HISTORIZED_WITH_ID = "Entity already historized with id: ";

    public static final String EM_ENTITY_NOT_FOUND_WITH_ID = "Entity not found with id: ";
    public static final String EM_ENTITY_ALREADY_EXISTS = "Entity already exists with id: ";
    public static final String EM_MISSING_ORDER_DIRECTION = "Missing orderDirection";
    public static final String EM_INSUFFICIENT_ORDER_DIRECTION = "Insufficient orderDirection";

    @Inject
    protected AbstractEntityRestSqlRepository<ENTITY> repository;

    @Inject
    protected AbstractEntityMapper<ENTITY, DTO> mapper;

    public DTO findById(String id) {
        ENTITY entity = repository.findById(Long.parseLong(id));
        if (entity == null) throw new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + id);
        return mapper.toDto(entity);
    }

    public DTO create(DTO dto) {
        ENTITY entity = repository.findById(Long.parseLong(dto.getId()));
        if (entity != null)
            throw new CustomException(Response.Status.BAD_REQUEST, EM_ENTITY_ALREADY_EXISTS + dto.getId());
        entity = mapper.toEntity(dto);
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
        repository.persist(entity);
        return mapper.toDto(entity);
    }

    @Transactional
    public DTO patch(DTO dto) {
        ENTITY entity = repository.findById(Long.parseLong(dto.getId()));
        if (entity == null)
            throw new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + dto.getId());
        if (entity instanceof AbstractEntityHistoricalSQL historicalEntity && dto instanceof AbstractHistoricalDTO historicalDTO) {
            //If it is already historicized, there was a conflict....
            if (historicalEntity.getEndValidityDate() != null) {
                throw new CustomException(Response.Status.CONFLICT, EM_ENTITY_ALREADY_HISTORIZED_WITH_ID + dto.getId());
            }
            //I apply the DTO: I use the end of validity date as closure of the effect...
            if (historicalDTO.getEndValidityDate() == null) {
                historicalDTO.setEndValidityDate(Instant.now());
            }
            historicalEntity.setEndValidityDate(historicalDTO.getEndValidityDate());

            AbstractEntityHistoricalSQL newEntity = (AbstractEntityHistoricalSQL) mapper.cloneToNewInstance(entity);
            mapper.patchToEntity(dto, (ENTITY) newEntity);
            newEntity.setStartValidityDate(historicalEntity.getEndValidityDate());
            newEntity.setId(null);
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
        repository.deleteById(Long.parseLong(id));
    }

    public <T> List<T> aggregate(List<FieldPair> groupBy, Map<AccumulatorType, FieldPair> aggregateBy, FindDTO findDTO, Class<T> clazz) {
        throw new NotImplementedException();
    }
}
