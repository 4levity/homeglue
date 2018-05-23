/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sim;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.upnp.BackgroundProcessHandle;
import net.forlevity.homeglue.upnp.SsdpSearcher;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Lo-fi simulation of a network with some devices that can be discovered via UPnP and contacted via HTTP.
 */
@Singleton
public class SimulatedNetwork implements SimpleHttpClient, SsdpSearcher {

    private final Collection<SimulatedNetworkDevice> devices;

    /**
     * Create a default network.
     */
    @Inject
    public SimulatedNetwork() {
        this.devices = new ArrayList<>();
        try {
            devices.add(new SimulatedUpnpRouter(InetAddress.getByName("192.168.6.1"), 5000));
            devices.add(new SimulatedUpnpMediaPlayer(InetAddress.getByName("192.168.6.119"), 8080));
            devices.add(new SimulatedWemo(InetAddress.getByName("192.168.6.231"), 49153, "net/forlevity/homeglue/sim/insight1_setup.xml"));
            devices.add(new SimulatedWemo(InetAddress.getByName("192.168.6.209"), 49154, "net/forlevity/homeglue/sim/insight2_setup.xml"));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a simulation with some devices.
     *
     * @param devices simulated devices
     */
    public SimulatedNetwork(Collection<SimulatedNetworkDevice> devices) {
        this.devices = devices;
    }

    @Override
    public String get(String url) throws IOException {
        return target(url).get(url);
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType) throws IOException {
        return target(url).post(url, headers, payload, contentType);
    }

    private SimulatedNetworkDevice target(String url) throws UnknownHostException {
        for (Iterator<SimulatedNetworkDevice> iterator = devices.iterator(); iterator.hasNext();) {
            SimulatedNetworkDevice candidate = iterator.next();
            if (url.startsWith(String.format("http://%s:", candidate.getInetAddress().getHostAddress()))) {
                return candidate;
            }
        }
        throw new UnknownHostException();
    }

    @Override
    public BackgroundProcessHandle startDiscovery(String serviceType, Consumer<SsdpServiceDefinition> serviceConsumer) {
        devices.forEach(device -> {
            if (device instanceof SsdpSearcher) {
                SsdpSearcher ssdpDevice = (SsdpSearcher) device;
                ssdpDevice.startDiscovery(serviceType, serviceConsumer);
            }
        });
        return () -> {};
    }
}
