package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.github.nashcrash.autorest.common.entity.ResultDTO;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;

import java.util.List;

public interface ReactiveResource<DTO> {
    @GET
    @Path("/{id}")
    Uni<DTO> getById(@PathParam("id") String id);

    @GET
    Uni<List<DTO>> search(@QueryParam("query") String query,
                          @QueryParam("orderBy") String[] orderBy,
                          @QueryParam("orderDirection") String[] orderDirection,
                          @QueryParam("page") int page,
                          @QueryParam("limit") int limit);

    @POST
    @Path("/find")
    Uni<List<DTO>> find(@Valid FindDTO dto);

    @POST
    @Path("/findAndCount")
    Uni<ResultDTO<DTO>> findAndCount(@Valid FindDTO dto);

    @POST
    Uni<DTO> create(@Valid DTO dto);

    @PUT
    Uni<DTO> upsert(@Valid DTO dto);

    @PATCH
    Uni<DTO> patch(@Valid DTO dto);

    @DELETE
    @Path("/{id}")
    Uni<Void> deleteById(@PathParam("id") String id);
}
