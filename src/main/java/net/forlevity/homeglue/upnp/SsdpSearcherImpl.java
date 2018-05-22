/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import com.google.inject.Singleton;
import io.resourcepool.ssdp.client.SsdpClient;
import io.resourcepool.ssdp.model.DiscoveryListener;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpService;
import io.resourcepool.ssdp.model.SsdpServiceAnnouncement;
import lombok.extern.log4j.Log4j2;

import java.util.function.Consumer;

/**
 * Standard implementation of SsdpSearcher.
 */
@Log4j2
@Singleton
public class SsdpSearcherImpl implements SsdpSearcher {

    @Override
    public BackgroundProcessHandle startDiscovery(String serviceType, Consumer<SsdpServiceDefinition> serviceConsumer) {
        SsdpClient client = SsdpClient.create();
        DiscoveryRequest.Builder discoveryRequest = DiscoveryRequest.builder();
        if (serviceType != null) {
            discoveryRequest.serviceType(serviceType);
        }
        client.discoverServices(discoveryRequest.build(), new DiscoveryListener() {

            @Override
            public void onServiceDiscovered(SsdpService service) {
                log.trace("onServiceDiscovered: {}", service);
                serviceConsumer.accept(new SsdpServiceDefinition(service));
            }

            @Override
            public void onServiceAnnouncement(SsdpServiceAnnouncement announcement) {
                log.debug("onServiceAnnouncement: {}", announcement);
            }

            @Override
            public void onFailed(Exception e) {
                log.warn("unexpected failure in SSDP discovery", e);
            }
        });
        return () -> client.stopDiscovery(); // return handle that allows stopping discovery
    }
}
