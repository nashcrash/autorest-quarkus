package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.AbstractDTO;
import io.github.nashcrash.autorest.common.entity.AbstractEntity;
import io.github.nashcrash.autorest.common.entity.FindDTO;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AbstractEntityRestResource<ENTITY extends AbstractEntity, DTO extends AbstractDTO> {
    @Inject
    protected AbstractEntityRestService<ENTITY, DTO> service;

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get a resource by ID"
    )
    public DTO getById(@PathParam("id") String id) {
        return service.findById(id);
    }

    @GET
    @Operation(
            summary = "Search resources with filtering and pagination"
    )
    public List<DTO> search(@QueryParam("query") String query,
                            @QueryParam("orderBy") String[] orderBy,
                            @QueryParam("orderDirection") String[] orderDirection,
                            @QueryParam("page") int page,
                            @QueryParam("limit") int limit) {
        return service.search(FindDTO.builder().query(query).orderBy(orderBy).orderDirection(orderDirection).page(page).limit(limit).build());
    }

    @POST
    @Path("/find")
    @Operation(
            summary = "Search resources with filtering and pagination"
    )
    public List<DTO> find(@Valid FindDTO dto) {
        return service.search(dto);
    }

    @POST
    @Operation(
            summary = "Create a new resource"
    )
    @APIResponse(
            responseCode = "201",
            description = "Resource created successfully"
    )
    public DTO create(@Valid DTO dto) {
        return service.create(dto);
    }

    @PUT
    @Operation(
            summary = "Upsert a resource"
    )
    @APIResponse(
            responseCode = "200",
            description = "Resource updated successfully"
    )
    public DTO upsert(@Valid DTO dto) {
        return service.upsert(dto);
    }

    @PATCH
    @Operation(
            summary = "Update only an existing resource, ignoring null fields"
    )
    @APIResponse(
            responseCode = "200",
            description = "Resource updated successfully"
    )
    public DTO patch(@Valid DTO dto) {
        return service.patch(dto);
    }

    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Delete a resource by ID"
    )
    public void deleteById(@PathParam("id") String id) {
        service.deleteById(id);
    }
}
