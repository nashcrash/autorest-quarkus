package io.github.nashcrash.autorest.common.context;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Provider
@Priority(Priorities.AUTHENTICATION)
@Slf4j
public class ContextFilter implements ContainerRequestFilter {
    @Inject
    ContextBean contextBean;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        for (var entry : requestContext.getHeaders().entrySet()) {
            if (!Objects.isNull(entry.getKey())) {
                String keyLowerCase = entry.getKey().toLowerCase();

                if (keyLowerCase.startsWith("x-") || keyLowerCase.equals("authorization") || keyLowerCase.startsWith("accept")) {
                    try {
                        contextBean.set(ContextHeader.fromString(keyLowerCase).getValue(), entry.getValue().get(0));
                    } catch (IllegalArgumentException e) {
                        contextBean.set(entry.getKey(), entry.getValue().get(0));
                    }
                }
            }
        }
        contextBean.set("path", requestContext.getMethod() + ":" + requestContext.getUriInfo().getPath());
    }
}
