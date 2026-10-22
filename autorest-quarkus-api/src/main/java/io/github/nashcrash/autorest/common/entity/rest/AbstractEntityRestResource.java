package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.AbstractDTO;
import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.github.nashcrash.autorest.common.entity.ResultDTO;
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
public class AbstractEntityRestResource<ENTITY extends AbstractEntity, DTO extends AbstractDTO> implements RestResource<DTO> {
    @Inject
    protected AbstractEntityRestService<ENTITY, DTO> service;

    public DTO getById(String id) {
        return service.findById(id);
    }

    public DTO getByBusinessKey(String businessKey) {
        return service.findByBusinessKey(businessKey);
    }

    public List<DTO> search(String query,
                            String[] orderBy,
                            String[] orderDirection,
                            int page,
                            int limit) {
        return service.search(FindDTO.builder().query(query).orderBy(orderBy).orderDirection(orderDirection).page(page).limit(limit).build());
    }

    public List<DTO> find(FindDTO dto) {
        return service.search(dto);
    }

    public ResultDTO<DTO> findAndCount(FindDTO dto) {
        ResultDTO<DTO> result = new ResultDTO<>();
        result.setTotalCount(service.count(dto));
        result.setElements(service.search(dto));
        result.setPage(dto.getPage());
        result.setLimit(dto.getLimit());
        return result;
    }

    @ResponseStatus(201)
    public DTO create(DTO dto) {
        return service.create(dto);
    }

    public DTO upsert(DTO dto) {
        return service.upsert(dto);
    }

    public DTO patch(DTO dto) {
        return service.patch(dto);
    }

    public DTO patchByBusinessKey(String businessKey, DTO dto) {
        dto.setBusinessKey(businessKey); //Ensure BusinessKey
        return service.patchByBusinessKey(businessKey, dto);
    }

    public void delete(String id) {
        service.deleteById(id);
    }

    public void deleteByBusinessKey(String businessKey) {
        service.deleteByBusinessKey(businessKey);
    }
}
