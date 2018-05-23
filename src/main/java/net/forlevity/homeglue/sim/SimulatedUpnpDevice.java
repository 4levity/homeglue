/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sim;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Synchronized;
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
public class SimulatedUpnpDevice extends BasicSimulatedNetworkDevice implements SsdpSearcher {

    protected final Xml xml = new Xml();
    protected final int upnpPort;

    private Collection<UpnpServiceInfo> services;
    private String location;
    private final Object configurationLock = new Object();

    /**
     * Construct a simulated UPnP device.
     *
     * @param inetAddress ip addr
     * @param upnpPort port
     * @param services service info
     * @param location url
     */
    SimulatedUpnpDevice(InetAddress inetAddress, int upnpPort,
                                  Collection<UpnpServiceInfo> services, String location) {
        super(inetAddress);
        this.upnpPort = upnpPort;
        this.services = services;
        this.location = location;
    }

    /**
     * Construct a simulated UPnP device where location is known but service definitions are not known yet.
     * Implementation should call setServices() later.
     *
     * @param inetAddress ip addr
     * @param upnpPort port
     * @param location url
     */
    SimulatedUpnpDevice(InetAddress inetAddress, int upnpPort, String location) {
        this(inetAddress, upnpPort, ImmutableList.of(), location);
    }

    /**
     * Construct a simulated UPnP device with known services and a default "/root.xml" location.
     *
     * @param inetAddress ip addr
     * @param upnpPort port
     * @param services service info
     */
    public SimulatedUpnpDevice(InetAddress inetAddress, int upnpPort,
                                  Collection<UpnpServiceInfo> services) {
        this(inetAddress, upnpPort, services,
                String.format("http://%s:%d/root.xml", inetAddress.getHostAddress(), upnpPort));
    }

    /**
     * Subclass calls to provide data for SSDP discovery if not provided with constructor.
     */
    @Synchronized("configurationLock")
    @VisibleForTesting
    public final void setServices(Collection<UpnpServiceInfo> services) {
        this.services = ImmutableList.copyOf(services);
    }

    /**
     * Subclass implements to provide SSDP location URL if not provided with constructor.
     */
    @Synchronized("configurationLock")
    protected final void setLocation(String location) {
        this.location = location;
    }

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
