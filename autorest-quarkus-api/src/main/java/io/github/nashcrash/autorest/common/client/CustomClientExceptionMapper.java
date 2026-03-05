package io.github.nashcrash.autorest.common.client;

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
        String body = null;
        if (response.getLength() > 0) {
            ByteArrayInputStream is = (ByteArrayInputStream) response.getEntity();
            byte[] bytes = new byte[is.available()];
            is.read(bytes, 0, is.available());
            body = new String(bytes);
        }
        return body;
    }

}
