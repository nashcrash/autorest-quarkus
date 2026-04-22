package io.github.nashcrash.autorest.common.client;

import io.github.nashcrash.autorest.common.context.ContextBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import java.util.Map;

@ApplicationScoped
public class CustomClientHeaderFactory implements ClientHeadersFactory {

    @Inject
    ContextBean contextBean;

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders, MultivaluedMap<String, String> clientOutgoingHeaders) {
        Map<String, String> allContext = contextBean.getAll();
        for (Map.Entry<String, String> context : allContext.entrySet()) {
            clientOutgoingHeaders.add(context.getKey(), context.getValue());
        }
        return clientOutgoingHeaders;
    }

}
