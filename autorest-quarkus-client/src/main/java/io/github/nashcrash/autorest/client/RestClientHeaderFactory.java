package io.github.nashcrash.autorest.client;

import io.github.nashcrash.autorest.common.context.ContextManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import java.util.Map;

@ApplicationScoped
public class RestClientHeaderFactory implements ClientHeadersFactory {

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        Map<String, String> allContext = ContextManager.getAllContext();
        for (Map.Entry<String, String> context : allContext.entrySet()) {
            clientOutgoingHeaders.add(context.getKey(), context.getValue());
        }
        return clientOutgoingHeaders;
    }

}
