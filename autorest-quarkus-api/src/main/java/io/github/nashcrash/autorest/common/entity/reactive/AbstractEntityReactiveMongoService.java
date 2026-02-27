package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.github.nashcrash.autorest.common.util.PipelineUtils;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Map;

@Slf4j
public class AbstractEntityReactiveMongoService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements AbstractEntityReactiveService<ENTITY, DTO> {
    public static final String EM_ENTITY_NOT_FOUND_WITH_ID = "Entity not found with id: ";
    public static final String EM_MISSING_ORDER_DIRECTION = "Missing orderDirection";
    public static final String EM_INSUFFICIENT_ORDER_DIRECTION = "Insufficient orderDirection";

    @Inject
    protected AbstractEntityReactiveMongoRepository<ENTITY> repository;

    @Inject
    protected AbstractEntityMapper<ENTITY, DTO> mapper;

    public Uni<DTO> findById(String id) {
        return repository.findById(id)
                .onItem().ifNull().failWith(new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + id))
                .onItem().transform(mapper::toDto);
    }

    public Uni<DTO> create(DTO dto) {
        ENTITY entity = mapper.toEntity(dto);
        return repository.persist(entity).replaceWith(() -> mapper.toDto(entity));
    }

    public Uni<List<DTO>> search(FindDTO findDTO) {
        Sort sort = findDTO.getSort();
        ReactivePanacheQuery<ENTITY> eventoReactivePanacheQuery;
        if (StringUtils.isNotBlank(findDTO.getQuery())) {
            eventoReactivePanacheQuery = repository.find(findDTO.getQuery(), sort);
        } else {
            eventoReactivePanacheQuery = repository.findAll(sort);
        }
        return eventoReactivePanacheQuery.page(findDTO.getPage(), findDTO.getLimit()).list()
                .onItem().transform(mapper::toDtos);
    }

    public Uni<DTO> upsert(DTO dto) {
        ENTITY entity = mapper.toEntity(dto);
        return repository.persistOrUpdate(entity).replaceWith(() -> mapper.toDto(entity));
    }

    public Uni<DTO> patch(DTO dto) {
        return repository.findById(dto.getId())
                .onItem().ifNotNull().transformToUni(entity -> {
                    mapper.patchToEntity(dto, entity);
                    return repository.persistOrUpdate(entity).map(v -> mapper.toDto(entity));
                })
                .onItem().ifNull().failWith(() -> new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + dto.getId()));
    }

    public Uni<Void> deleteById(String id) {
        return repository.deleteById(id).replaceWithVoid();
    }

    public <T> Uni<List<T>> aggregate(List<FieldPair> groupBy, Map<AccumulatorType, FieldPair> aggregateBy, FindDTO findDTO, Class<T> clazz) {
        List<Bson> pipeline = PipelineUtils.aggregate(groupBy, aggregateBy, findDTO);
        return this.repository.mongoCollection().aggregate(pipeline, clazz).collect().asList();
    }
}
