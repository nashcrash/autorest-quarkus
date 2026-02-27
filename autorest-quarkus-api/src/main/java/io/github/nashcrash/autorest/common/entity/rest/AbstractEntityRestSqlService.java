package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.AbstractDTO;
import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.github.nashcrash.autorest.common.entity.AbstractEntityMapper;
import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Slf4j
public class AbstractEntityRestSqlService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements AbstractEntityRestService<ENTITY, DTO> {
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
        repository.persist(entity);
        return mapper.toDto(entity);
    }

    public DTO patch(DTO dto) {
        ENTITY entity = repository.findById(Long.parseLong(dto.getId()));
        if (entity == null)
            throw new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + dto.getId());
        mapper.patchToEntity(dto, entity);
        repository.persist(entity);
        return mapper.toDto(entity);
    }

    public void deleteById(String id) {
        repository.deleteById(Long.parseLong(id));
    }
}
