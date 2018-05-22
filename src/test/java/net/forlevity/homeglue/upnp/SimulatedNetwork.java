/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpService;
import net.forlevity.homeglue.http.SimpleHttpClient;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class SimulatedNetwork implements SimpleHttpClient, SsdpSearcher {



    @Override
    public String get(String url) throws IOException {
        return null;
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType) throws IOException {
        return null;
    }

    @Override
    public BackgroundProcessHandle startDiscovery(DiscoveryRequest discoveryRequest, Consumer<SsdpService> serviceConsumer) {
        return null;
    }
}
