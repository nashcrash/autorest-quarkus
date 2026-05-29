package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.*;
import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Map;

public interface AbstractEntityRestService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> {

    DTO findById(String id);

    List<DTO> search(FindDTO findDTO);

    Long count(FindDTO findDTO);

    DTO create(DTO dto);

    DTO upsert(DTO dto);

    DTO patch(DTO dto);

    void deleteById(String id);

    <T> List<T> aggregate(List<FieldPair> groupBy, List<FieldMap> aggregateBy, FindDTO findDTO, Class<T> clazz);
    <T> List<T> aggregate(List<FieldPair> groupBy, List<FieldMap> aggregateBy, String elementField, String unwindFields, FindDTO findDTO, Class<T> clazz);

    <T> ResultDTO<T> aggregateAndCount(List<FieldPair> groupBy, List<FieldMap> aggregateBy, String elementField, String unwindFields, FindDTO findDTO, Class<T> clazz);
}
