/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.generic_upnp;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.resourcepool.ssdp.model.SsdpService;
import net.forlevity.homeglue.device.AbstractSoapDeviceConnector;
import net.forlevity.homeglue.http.SimpleHttpClient;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class GenericUpnpConnector extends AbstractSoapDeviceConnector {

    private final InetAddress hostAddress;
    private final Set<SsdpService> ssdpServices = new HashSet<>();

    @Inject
    protected GenericUpnpConnector(SimpleHttpClient httpClient,
                                   @Assisted InetAddress hostAddress) {
        super(httpClient);
        this.hostAddress = hostAddress;
    }

    @Override
    public boolean connect() {
        return true;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    public void add(SsdpService service) {
        synchronized (ssdpServices) {
            ssdpServices.add(service);
        }
    }

    public boolean has(SsdpService service) {
        synchronized (ssdpServices) {
            return ssdpServices.contains(service);
        }
    }
}