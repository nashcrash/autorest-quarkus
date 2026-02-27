package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.AbstractDTO;
import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.github.nashcrash.autorest.common.entity.FindDTO;

import java.util.List;

public interface AbstractEntityRestService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> {

    DTO findById(String id);

    List<DTO> search(FindDTO findDTO);

    DTO create(DTO dto);

    DTO upsert(DTO dto);

    DTO patch(DTO dto);

    void deleteById(String id);
}
