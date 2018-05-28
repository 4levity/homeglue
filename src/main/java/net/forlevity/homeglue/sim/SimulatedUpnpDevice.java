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
import net.forlevity.homeglue.upnp.SafeCloseable;
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

    private Collection<UpnpServiceInfo> services;
    private final Object configurationLock = new Object();

    /**
     * Construct a simulated UPnP device.
     *
     * @param inetAddress ip addr
     * @param webPort port
     * @param services service info
     */
    public SimulatedUpnpDevice(InetAddress inetAddress, int webPort, Collection<UpnpServiceInfo> services) {
        super(inetAddress, webPort);
        this.services = services;
    }

    /**
     * Construct a simulated UPnP device where location is known but service definitions are not known yet.
     * Implementation should call setServices() later.
     *
     * @param inetAddress ip addr
     * @param webPort port
     */
    SimulatedUpnpDevice(InetAddress inetAddress, int webPort) {
        this(inetAddress, webPort, ImmutableList.of());
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
     * Subclass overrides this method to provide location. Note that port or address could change.
     *
     * @return location string
     */
    protected String getLocation() {
        return String.format("http://%s:%d/root.xml", getInetAddress().getHostAddress(), getWebPort());
    }

    @Override
    public SafeCloseable startDiscovery(String serviceType,
                                        Consumer<SsdpServiceDefinition> serviceConsumer) {
        // create mock service and send to consumer
        getServices().forEach(serviceMock -> {
            SsdpServiceDefinition serviceDefinition = new SsdpServiceDefinition(
                            serviceMock.getUsn(), serviceMock.getServiceType(), getLocation(), getInetAddress());
            serviceConsumer.accept(serviceDefinition);
        });
        return () -> {};
    }
}
