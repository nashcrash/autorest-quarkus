package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.AbstractDTO;
import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.github.nashcrash.autorest.common.entity.ResultDTO;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.List;

@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AbstractEntityReactiveResource<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements ReactiveResource<DTO> {
    @Inject
    protected AbstractEntityReactiveService<ENTITY, DTO> service;

    public Uni<DTO> getById(String id) {
        return service.findById(id);
    }

    public Uni<DTO> getByBusinessKey(String businessKey) {
        return service.findByBusinessKey(businessKey);
    }

    public Uni<List<DTO>> search(String query,
                                 String[] orderBy,
                                 String[] orderDirection,
                                 int page,
                                 int limit) {
        return service.search(FindDTO.builder().query(query).orderBy(orderBy).orderDirection(orderDirection).page(page).limit(limit).build());
    }

    public Uni<List<DTO>> find(FindDTO dto) {
        return service.search(dto);
    }

    public Uni<ResultDTO<DTO>> findAndCount(FindDTO dto) {
        return Uni.combine().all().unis(
                        service.search(dto),
                        service.count(dto)
                ).asTuple()
                .map(tuple -> {
                    ResultDTO<DTO> result = new ResultDTO<>();
                    result.setElements(tuple.getItem1());
                    result.setTotalCount(tuple.getItem2());
                    result.setPage(dto.getPage());
                    result.setLimit(dto.getLimit());
                    return result;
                });
    }

    @ResponseStatus(201)
    public Uni<DTO> create(DTO dto) {
        return service.create(dto);
    }

    public Uni<DTO> upsert(DTO dto) {
        return service.upsert(dto);
    }

    public Uni<DTO> patch(DTO dto) {
        return service.patch(dto);
    }

    public Uni<Void> deleteByBusinessKey(String businessKey) {
        return service.deleteByBusinessKey(businessKey);
    }
}
