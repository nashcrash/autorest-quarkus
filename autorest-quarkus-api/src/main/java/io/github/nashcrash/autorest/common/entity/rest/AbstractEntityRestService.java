package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.*;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.Map;

public interface AbstractEntityRestService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> {

    DTO findById(String id);

    List<DTO> search(FindDTO findDTO);

    DTO create(DTO dto);

    DTO upsert(DTO dto);

    DTO patch(DTO dto);

    void deleteById(String id);

    <T> List<T> aggregate(List<FieldPair> groupBy, Map<AccumulatorType, FieldPair> aggregateBy, FindDTO findDTO, Class<T> clazz);
}
