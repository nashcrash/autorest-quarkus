package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.*;

import java.util.List;

public interface AbstractEntityRestService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> {

    /**
     * Looks up a single version by its technical id.
     */
    DTO findById(String id);

    /**
     * Looks up the entity by its stable business key: the current version (null endValidityDate)
     * for a historical entity, the entity itself otherwise.
     */
    DTO findByBusinessKey(String businessKey);

    List<DTO> search(FindDTO findDTO);

    Long count(FindDTO findDTO);

    DTO create(DTO dto);

    DTO upsert(DTO dto);

    DTO patch(DTO dto);

    DTO patchByBusinessKey(String businessKey, DTO dto);

    /**
     * Physically deletes a single version. Not exposed by the generated REST API: expose it from
     * your own controller if you need it.
     */
    void deleteById(String id);

    /**
     * Closes the current version of a historical entity (endValidityDate = now), keeping its history;
     * physically deletes a non-historical entity.
     */
    void deleteByBusinessKey(String businessKey);

    <T> List<T> aggregate(List<FieldPair> groupBy, List<FieldMap> aggregateBy, String elementField, String unwindFields, FindDTO findDTO, Class<T> clazz);

    <T> ResultDTO<T> aggregateAndCount(List<FieldPair> groupBy, List<FieldMap> aggregateBy, String elementField, String unwindFields, FindDTO findDTO, Class<T> clazz);
}
