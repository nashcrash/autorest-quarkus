package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.*;
import io.github.nashcrash.autorest.common.exception.CustomException;
import io.github.nashcrash.autorest.common.util.PipelineUtils;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
public class AbstractEntityReactiveSqlService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements AbstractEntityReactiveService<ENTITY, DTO> {
    public static final String EM_ENTITY_ALREADY_HISTORIZED_WITH_ID = "Entity already historized with id: ";
    public static final String EM_ENTITY_NOT_FOUND_WITH_ID = "Entity not found with id: ";
    public static final String EM_MISSING_ORDER_DIRECTION = "Missing orderDirection";
    public static final String EM_INSUFFICIENT_ORDER_DIRECTION = "Insufficient orderDirection";

    @Inject
    protected AbstractEntityReactiveSqlRepository<ENTITY> repository;

    @Inject
    protected AbstractEntityMapper<ENTITY, DTO> mapper;

    public Uni<DTO> findById(String id) {
        return repository.findById(Long.parseLong(id))
                .onItem().ifNull().failWith(new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + id))
                .onItem().transform(mapper::toDto);
    }

    public Uni<DTO> create(DTO dto) {
        ENTITY entity = mapper.toEntity(dto);
        return repository.persist(entity).replaceWith(() -> mapper.toDto(entity));
    }

    public Uni<List<DTO>> search(FindDTO findDTO) {
        Sort sort = PipelineUtils.getSort(findDTO);
        PanacheQuery<ENTITY> eventoReactivePanacheQuery;
        if (StringUtils.isNotBlank(findDTO.getQuery())) {
            eventoReactivePanacheQuery = repository.find(findDTO.getQuery(), sort);
        } else {
            eventoReactivePanacheQuery = repository.findAll(sort);
        }
        return eventoReactivePanacheQuery.page(findDTO.getPage(), findDTO.getLimit()).list()
                .onItem().transform(mapper::toDtos);
    }

    public Uni<Long> count(FindDTO findDTO) {
        if (StringUtils.isNotBlank(findDTO.getQuery())) {
            return repository.count(findDTO.getQuery());
        } else {
            return repository.count();
        }
    }

    public Uni<DTO> upsert(DTO dto) {
        ENTITY entity = mapper.toEntity(dto);
        return repository.persist(entity).replaceWith(() -> mapper.toDto(entity));
    }

    public Uni<DTO> patch(DTO dto) {
        return Panache.withTransaction(() -> repository.findById(Long.parseLong(dto.getId()))
                .onItem().ifNotNull().transformToUni(entity -> {
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

                        return Uni.combine().all().unis(
                                        repository.persist((ENTITY) historicalEntity),
                                        repository.persist((ENTITY) newEntity)
                                ).asTuple()
                                .map(tuple -> mapper.toDto((ENTITY) newEntity));
                    } else {
                        return repository.persist(entity).map(v -> mapper.toDto(entity));
                    }
                })
                .onItem().ifNull().failWith(() -> new CustomException(Response.Status.NOT_FOUND, EM_ENTITY_NOT_FOUND_WITH_ID + dto.getId()))
        );
    }

    public Uni<Void> deleteById(String id) {
        return repository.deleteById(Long.parseLong(id)).replaceWithVoid();
    }

    public <T> Uni<List<T>> aggregate(List<FieldPair> groupBy, Map<AccumulatorType, FieldPair> aggregateBy, FindDTO findDTO, Class<T> clazz) {
        throw new NotImplementedException();
    }
}
