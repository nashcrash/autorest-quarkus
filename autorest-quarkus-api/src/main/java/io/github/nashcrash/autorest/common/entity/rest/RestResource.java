package io.github.nashcrash.autorest.common.entity.rest;

import io.github.nashcrash.autorest.common.entity.FindDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

public interface RestResource<DTO> {
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get a resource by ID"
    )
    DTO getById(@PathParam("id") String id);

    @GET
    @Operation(
            summary = "Search resources with filtering and pagination"
    )
    List<DTO> search(@QueryParam("query") String query,
                     @QueryParam("orderBy") String[] orderBy,
                     @QueryParam("orderDirection") String[] orderDirection,
                     @QueryParam("page") int page,
                     @QueryParam("limit") int limit);

    @POST
    @Path("/find")
    @Operation(
            summary = "Search resources with filtering and pagination"
    )
    List<DTO> find(@Valid FindDTO dto);

    @POST
    @Operation(
            summary = "Create a new resource"
    )
    @APIResponse(
            responseCode = "201",
            description = "Resource created successfully"
    )
    DTO create(@Valid DTO dto);

    @PUT
    @Operation(
            summary = "Upsert a resource"
    )
    @APIResponse(
            responseCode = "200",
            description = "Resource updated successfully"
    )
    DTO upsert(@Valid DTO dto);

    @PATCH
    @Operation(
            summary = "Update only an existing resource, ignoring null fields"
    )
    @APIResponse(
            responseCode = "200",
            description = "Resource updated successfully"
    )
    DTO patch(@Valid DTO dto);

    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Delete a resource by ID"
    )
    void deleteById(@PathParam("id") String id);
}
