package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.*;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface AbstractEntityReactiveService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> {
    /**
     * Looks up a single version by its technical id.
     */
    Uni<DTO> findById(String id);

    /**
     * Looks up the entity by its stable business key: the current version (null endValidityDate)
     * for a historical entity, the entity itself otherwise.
     */
    Uni<DTO> findByBusinessKey(String businessKey);

    Uni<List<DTO>> search(FindDTO findDTO);

    Uni<Long> count(FindDTO findDTO);

    Uni<DTO> create(DTO dto);

    Uni<DTO> upsert(DTO dto);

    Uni<DTO> patch(DTO dto);

    Uni<DTO> patchByBusinessKey(String businessKey, DTO dto);

    /**
     * Physically deletes a single version. Not exposed by the generated REST API: expose it from
     * your own controller if you need it.
     */
    Uni<Void> deleteById(String id);

    /**
     * Closes the current version of a historical entity (endValidityDate = now), keeping its history;
     * physically deletes a non-historical entity.
     */
    Uni<Void> deleteByBusinessKey(String businessKey);

    <T> Uni<List<T>> aggregate(List<FieldPair> groupBy, List<FieldMap> aggregateBy, String elementField, String unwindFields, FindDTO findDTO, Class<T> clazz);

    <T> Uni<ResultDTO<T>> aggregateAndCount(List<FieldPair> groupBy, List<FieldMap> aggregateBy, String elementField, String unwindFields, FindDTO findDTO, Class<T> clazz);
}
