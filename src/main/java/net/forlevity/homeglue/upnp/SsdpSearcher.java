/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import com.google.inject.ImplementedBy;

import java.util.function.Consumer;

/**
 * Interface wrapping resourcepool.io ssdp-client.
 */
@ImplementedBy(SsdpSearcherImpl.class)
public interface SsdpSearcher {

    String ROOT_DEVICE_SERVICE_TYPE = "upnp:rootdevice";

    /**
     * Start an SSDP broadcast discovery. As services are discovered, they will be passed to a consumer. The consumer
     * should be fast. Caller must retain the SafeCloseable handle and call handle.close() after a few seconds.
     *
     * @param serviceType specific service type e.g. "upnp:rootdevice", or null for 'all'
     * @param serviceConsumer sink for service information
     * @return handle to stop discovery
     */
    SafeCloseable startDiscovery(String serviceType, Consumer<SsdpServiceDefinition> serviceConsumer);
}
