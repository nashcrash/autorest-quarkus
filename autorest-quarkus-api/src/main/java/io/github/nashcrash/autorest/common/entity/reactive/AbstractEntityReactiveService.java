package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.AbstractDTO;
import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.smallrye.mutiny.Uni;

import java.util.List;

public interface AbstractEntityReactiveService<ENTITY extends AbstractEntity, DTO extends AbstractDTO> {
    Uni<DTO> findById(String id);

    Uni<List<DTO>> search(FindDTO findDTO);

    Uni<DTO> create(DTO dto);

    Uni<DTO> upsert(DTO dto);

    Uni<DTO> patch(DTO dto);

    Uni<Void> deleteById(String id);
}
