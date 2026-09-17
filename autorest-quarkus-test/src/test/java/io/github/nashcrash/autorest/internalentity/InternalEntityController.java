package io.github.nashcrash.autorest.internalentity;

import io.github.nashcrash.autorest.common.entity.FindDTO;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.util.List;

@Slf4j
@Path("/internal_entity")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class InternalEntityController {
    @Inject
    InternalEntityService service;

    @GET
    @Operation(
            summary = "Search resources with filtering and pagination"
    )
    public List<InternalEntityDTO> search(@QueryParam("query") String query,
                                 @QueryParam("orderBy") String[] orderBy,
                                 @QueryParam("orderDirection") String[] orderDirection,
                                 @QueryParam("page") int page,
                                 @QueryParam("limit") int limit) {
        return service.search(FindDTO.builder().query(query).orderBy(orderBy).orderDirection(orderDirection).page(page).limit(limit).build());
    }
}
