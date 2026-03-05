package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.github.nashcrash.autorest.common.util.PipelineUtils;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
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
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
public class AbstractEntityReactiveMongoService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements AbstractEntityReactiveService<ENTITY, DTO> {
    public static final String EM_ENTITY_ALREADY_HISTORIZED_WITH_ID = "Entity already historized with id: ";
    public static final String EM_ENTITY_NOT_FOUND_WITH_ID = "Entity not found with id: ";
    public static final String EM_MISSING_ORDER_DIRECTION = "Missing orderDirection";
    public static final String EM_INSUFFICIENT_ORDER_DIRECTION = "Insufficient orderDirection";

    @Inject
    @Getter
    @Setter
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
        Sort sort = PipelineUtils.getSort(findDTO);
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
        return Panache.withTransaction(() -> repository.findById(dto.getId())
                .onItem().ifNotNull().transformToUni(entity -> {
                    mapper.patchToEntity(dto, entity);
                    if (entity instanceof AbstractEntityHistoricalMongo historicalEntity) {
                        if (historicalEntity.getEndValidityDate() != null) {
                            throw new CustomException(Response.Status.CONFLICT, EM_ENTITY_ALREADY_HISTORIZED_WITH_ID + dto.getId());
                        }
                        historicalEntity.setEndValidityDate(Instant.now());

                        AbstractEntityHistoricalMongo newEntity = (AbstractEntityHistoricalMongo) mapper.cloneToNewInstance(entity);
                        newEntity.setStartValidityDate(historicalEntity.getEndValidityDate());
                        newEntity.setEndValidityDate(null);
                        newEntity.setInsertionUser(null);
                        newEntity.setInsertionDate(null);
                        newEntity.setLastModifiedUser(null);
                        newEntity.setLastModifiedDate(null);
                        mapper.addExtraEntityData((ENTITY) newEntity);

                        return Uni.combine().all().unis(
                                        repository.update((ENTITY) historicalEntity),
                                        repository.persist((ENTITY) newEntity)
                                ).asTuple()
                                .map(tuple -> mapper.toDto((ENTITY) newEntity));
                    } else {
                        return repository.persistOrUpdate(entity).map(v -> mapper.toDto(entity));
                    }
                })
                .onItem().ifNull().failWith(() -> new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + dto.getId()))
        );
    }

    public Uni<Void> deleteById(String id) {
        return repository.deleteById(id).replaceWithVoid();
    }

    public <T> Uni<List<T>> aggregate(List<FieldPair> groupBy, Map<AccumulatorType, FieldPair> aggregateBy, FindDTO findDTO, Class<T> clazz) {
        List<Bson> pipeline = PipelineUtils.aggregate(groupBy, aggregateBy, findDTO);
        return this.repository.mongoCollection().aggregate(pipeline, clazz).collect().asList();
    }
}
