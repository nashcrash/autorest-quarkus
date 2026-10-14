package io.github.nashcrash.autorest.common.entity.reactive;

import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.github.nashcrash.autorest.common.entity.ResultDTO;
import io.github.nashcrash.autorest.common.validator.groups.FindValidator;
import io.github.nashcrash.autorest.common.validator.groups.PatchValidator;
import io.github.nashcrash.autorest.common.validator.groups.PostValidator;
import io.github.nashcrash.autorest.common.validator.groups.PutValidator;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.validation.groups.Default;
import jakarta.ws.rs.*;
import org.jboss.resteasy.reactive.ResponseStatus;

import java.util.List;

public interface ReactiveResource<DTO> {
    @GET
    @Path("/{id}")
    Uni<DTO> getById(@PathParam("id") String id);

    @GET
    @Path("/businessKey/{businessKey}")
    Uni<DTO> getByBusinessKey(@PathParam("businessKey") String businessKey);

    @GET
    Uni<List<DTO>> search(@QueryParam("query") String query,
                          @QueryParam("orderBy") String[] orderBy,
                          @QueryParam("orderDirection") String[] orderDirection,
                          @QueryParam("page") int page,
                          @QueryParam("limit") int limit);

    @POST
    @Path("/find")
    Uni<List<DTO>> find(@Valid @ConvertGroup(from = Default.class, to = FindValidator.class) FindDTO dto);

    @POST
    @Path("/findAndCount")
    Uni<ResultDTO<DTO>> findAndCount(@Valid @ConvertGroup(from = Default.class, to = FindValidator.class) FindDTO dto);

    @POST
    @ResponseStatus(201)
    Uni<DTO> create(@Valid @ConvertGroup(from = Default.class, to = PostValidator.class) DTO dto);

    @PUT
    Uni<DTO> upsert(@Valid @ConvertGroup(from = Default.class, to = PutValidator.class) DTO dto);

    @PATCH
    Uni<DTO> patch(@Valid @ConvertGroup(from = Default.class, to = PatchValidator.class) DTO dto);

    @DELETE
    @Path("/businessKey/{businessKey}")
    Uni<Void> deleteByBusinessKey(@PathParam("businessKey") String businessKey);
}
