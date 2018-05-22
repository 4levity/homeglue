/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import com.google.inject.ImplementedBy;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpService;

import java.util.function.Consumer;

/**
 * Interface wrapping resourcepool.io ssdp-client.
 */
@ImplementedBy(SsdpSearcherImpl.class)
public interface SsdpSearcher {

    /**
     * Start an SSDP broadcast discovery. As services are discovered, they will be passed to a consumer. The consumer
     * should be fast. Caller must retain the BackgroundProcessHandle and call handle.stop() after a few seconds.
     *
     * @param discoveryRequest the request e.g. DiscoveryRequest.discoverAll()
     * @param serviceConsumer sink for service information
     * @return handle to stop discovery
     */
    BackgroundProcessHandle startDiscovery(DiscoveryRequest discoveryRequest, Consumer<SsdpService> serviceConsumer);
}
