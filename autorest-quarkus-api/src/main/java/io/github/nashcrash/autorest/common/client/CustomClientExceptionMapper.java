package io.github.nashcrash.autorest.common.client;

import io.quarkus.runtime.Quarkus;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import java.io.ByteArrayInputStream;

@Provider
public class CustomClientExceptionMapper implements ResponseExceptionMapper<RuntimeException> {

    @Override
    public RuntimeException toThrowable(Response response) {
        String msg = getBody(response);
        return new WebApplicationException(msg, response);
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status >= 400;
    }

    private String getBody(Response response) {
        try {
            if (response.hasEntity()) {
                return response.readEntity(String.class);
            }
        } catch (Exception e) {
            return "Error while parsing remote response body: " + e.getMessage();
        }
        return "No response body available";
    }

}
