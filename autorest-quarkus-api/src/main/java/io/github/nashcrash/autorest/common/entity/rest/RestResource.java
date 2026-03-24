package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.github.nashcrash.autorest.common.entity.ResultDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

public interface RestResource<DTO> {
    @GET
    @Path("/{id}")
    DTO getById(@PathParam("id") String id);

    @GET
    List<DTO> search(@QueryParam("query") String query,
                     @QueryParam("orderBy") String[] orderBy,
                     @QueryParam("orderDirection") String[] orderDirection,
                     @QueryParam("page") int page,
                     @QueryParam("limit") int limit);

    @POST
    @Path("/find")
    List<DTO> find(@Valid FindDTO dto);

    @POST
    @Path("/findAndCount")
    ResultDTO<DTO> findAndCount(@Valid FindDTO dto);

    @POST
    DTO create(@Valid DTO dto);

    @PUT
    DTO upsert(@Valid DTO dto);

    @PATCH
    DTO patch(@Valid DTO dto);

    @DELETE
    @Path("/{id}")
    void deleteById(@PathParam("id") String id);
}
