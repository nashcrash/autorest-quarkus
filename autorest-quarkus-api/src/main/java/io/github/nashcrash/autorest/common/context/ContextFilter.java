package io.github.nashcrash.autorest.common.context;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Provider
@Priority(Priorities.USER)
@Slf4j
public class ContextFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        ContextManager.removeContext();
        for (var entry : requestContext.getHeaders().entrySet()) {
            if (!Objects.isNull(entry.getKey())) {
                String keyLowerCase = entry.getKey().toLowerCase();

                if (keyLowerCase.startsWith("x-") || keyLowerCase.equals("authorization") || keyLowerCase.equals("accept")) {
                    try {
                        ContextManager.setParameter(ContextHeader.fromString(keyLowerCase).getValue(), entry.getValue().get(0));
                    } catch (IllegalArgumentException e) {
                        ContextManager.setParameter(entry.getKey(), entry.getValue().get(0));
                    }
                }
            }
        }
        ContextManager.setParameter("path", requestContext.getMethod() + ":" + requestContext.getUriInfo().getPath());
    }
}
