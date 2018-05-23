/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sim;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.upnp.BackgroundProcessHandle;
import net.forlevity.homeglue.upnp.SsdpSearcher;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;
import net.forlevity.homeglue.util.Xml;

import java.net.InetAddress;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Lo-fi simulation of a network device that advertises services over SSDP.
 */
@Log4j2
@Getter
public abstract class AbstractSimulatedUpnpDevice extends AbstractSimulatedNetworkDevice implements SsdpSearcher {

    protected final Xml xml = new Xml();
    protected final int upnpPort;

    protected AbstractSimulatedUpnpDevice(InetAddress inetAddress, int upnpPort) {
        super(inetAddress);
        this.upnpPort = upnpPort;
    }

    /**
     * Subclass implements to provide data for SSDP discovery.
     *
     * @return list of services
     */
    protected abstract Collection<UpnpServiceInfo> getServices();

    /**
     * Subclass implements to provide SSDP location URL.
     * 
     * @return url
     */
    protected abstract String getLocation();

    @Override
    public BackgroundProcessHandle startDiscovery(String serviceType,
                                                  Consumer<SsdpServiceDefinition> serviceConsumer) {
        // create mock service and send to consumer
        getServices().forEach(serviceMock -> {
            SsdpServiceDefinition serviceDefinition = new SsdpServiceDefinition(
                            serviceMock.getUsn(), serviceMock.getServiceType(), getLocation(), inetAddress);
            serviceConsumer.accept(serviceDefinition);
        });
        return () -> {};
    }
}
