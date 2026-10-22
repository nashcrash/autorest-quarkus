package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.github.nashcrash.autorest.common.entity.ResultDTO;
import io.github.nashcrash.autorest.common.validator.groups.FindValidator;
import io.github.nashcrash.autorest.common.validator.groups.PatchValidator;
import io.github.nashcrash.autorest.common.validator.groups.PostValidator;
import io.github.nashcrash.autorest.common.validator.groups.PutValidator;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;
import jakarta.ws.rs.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.List;

public interface ReactiveResource<DTO> {
    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get a resource by ID"
    )
    Uni<DTO> getById(@PathParam("id") String id);

    @GET
    @Path("/businessKey/{businessKey}")
    @Operation(
            summary = "Get a resource by business key: the current version of a historical entity"
    )
    Uni<DTO> getByBusinessKey(@PathParam("businessKey") String businessKey);

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
    Uni<List<DTO>> find(@Valid @ConvertGroup(from = Default.class, to = FindValidator.class) FindDTO dto);

    @POST
    @Path("/findAndCount")
    @Operation(
            summary = "Search resources with filtering and pagination with totalCount"
    )
    Uni<ResultDTO<DTO>> findAndCount(@Valid @ConvertGroup(from = Default.class, to = FindValidator.class) FindDTO dto);

    @POST
    @Operation(
            summary = "Create a new resource"
    )
    @APIResponse(
            responseCode = "201",
            description = "Resource created successfully"
    )
    @ResponseStatus(201)
    Uni<DTO> create(@Valid @ConvertGroup(from = Default.class, to = PostValidator.class) DTO dto);

    @PUT
    @Operation(
            summary = "Upsert a resource"
    )
    @APIResponse(
            responseCode = "200",
            description = "Resource updated successfully"
    )
    Uni<DTO> upsert(@Valid @ConvertGroup(from = Default.class, to = PutValidator.class) DTO dto);

    @PATCH
    @Blocking
    @Operation(
            summary = "Update only an existing resource, ignoring null fields"
    )
    @APIResponse(
            responseCode = "200",
            description = "Resource updated successfully"
    )
    Uni<DTO> patch(@Valid @ConvertGroup(from = Default.class, to = PatchValidator.class) DTO dto);

    @PATCH
    @Path("/businessKey/{businessKey}")
    @Blocking
    @Operation(
            summary = "Update only an existing resource, ignoring null fields"
    )
    @APIResponse(
            responseCode = "200",
            description = "Resource updated successfully"
    )
    Uni<DTO> patchByBusinessKey(@PathParam("businessKey") String businessKey, @Valid @ConvertGroup(from = Default.class, to = PatchValidator.class) DTO dto);

    @DELETE
    @Path("/{id}")
    @Operation(
            summary = "Delete a resource by id: a historical entity is closed, not removed"
    )
    Uni<Void> delete(@PathParam("id") String id);

    @DELETE
    @Path("/businessKey/{businessKey}")
    @Operation(
            summary = "Delete a resource by business key: a historical entity is closed, not removed"
    )
    Uni<Void> deleteByBusinessKey(@PathParam("businessKey") String businessKey);
}
