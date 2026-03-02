package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

public interface ReactiveResource<DTO> {
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get a resource by ID"
    )
    Uni<DTO> getById(@PathParam("id") String id);

    @GET
    @Operation(
            summary = "Search resources with filtering and pagination"
    )
    Uni<List<DTO>> search(@QueryParam("query") String query,
                          @QueryParam("orderBy") String[] orderBy,
                          @QueryParam("orderDirection") String[] orderDirection,
                          @QueryParam("page") int page,
                          @QueryParam("limit") int limit);

    @POST
    @Path("/find")
    @Operation(
            summary = "Search resources with filtering and pagination"
    )
    Uni<List<DTO>> find(@Valid FindDTO dto);

    @POST
    @Operation(
            summary = "Create a new resource"
    )
    @APIResponse(
            responseCode = "201",
            description = "Resource created successfully"
    )
    Uni<DTO> create(@Valid DTO dto);

    @PUT
    @Operation(
            summary = "Upsert a resource"
    )
    @APIResponse(
            responseCode = "200",
            description = "Resource updated successfully"
    )
    Uni<DTO> upsert(@Valid DTO dto);

    @PATCH
    @Operation(
            summary = "Update only an existing resource, ignoring null fields"
    )
    @APIResponse(
            responseCode = "200",
            description = "Resource updated successfully"
    )
    Uni<DTO> patch(@Valid DTO dto);

    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Delete a resource by ID"
    )
    Uni<Void> deleteById(@PathParam("id") String id);
}
