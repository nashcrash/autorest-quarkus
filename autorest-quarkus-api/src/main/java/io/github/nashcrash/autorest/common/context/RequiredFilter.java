package io.github.nashcrash.autorest.common.context;

import io.github.nashcrash.autorest.common.exception.CustomException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Provider
@Priority(Priorities.USER)
@Slf4j
public class RequiredFilter implements ContainerRequestFilter {
    @ConfigProperty(name = "required.header")
    Optional<Map<String, String>> requiredHeaders;

    @ConfigProperty(name = "required.param")
    Optional<Map<String, String>> requiredParam;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        UriInfo uriInfo = requestContext.getUriInfo();
        String currentPath = requestContext.getMethod() + ":" + uriInfo.getPath();
        checkHeaders(currentPath, requestContext.getHeaders());
        checkParams(currentPath, uriInfo.getQueryParameters());
    }

    private void checkHeaders(String currentPath, MultivaluedMap<String, String> headers) {
        if (requiredHeaders.isPresent()) {
            for (Map.Entry<String, String> entry : requiredHeaders.get().entrySet()) {
                if (currentPath.matches(entry.getValue())
                        && (!headers.containsKey(entry.getKey())
                        || StringUtils.isBlank(headers.get(entry.getKey()).getFirst()))
                ) {
                    throw new CustomException(Response.Status.BAD_REQUEST, "Missing required header: " + entry.getKey());
                }
            }
        }
    }

    private void checkParams(String currentPath, MultivaluedMap<String, String> params) {
        if (requiredParam.isPresent()) {
            for (Map.Entry<String, String> entry : requiredParam.get().entrySet()) {
                if (currentPath.matches(entry.getValue())
                        && (!params.containsKey(entry.getKey())
                        || StringUtils.isBlank(params.get(entry.getKey()).getFirst()))
                ) {
                    throw new CustomException(Response.Status.BAD_REQUEST, "Missing required parameter: " + entry.getKey());
                }
            }
        }
    }
}
