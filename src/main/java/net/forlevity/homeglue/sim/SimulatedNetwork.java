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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Singleton
public class SimulatedNetwork implements SimpleHttpClient, SsdpSearcher {

    private final List<SimulatedWemo> simulatedWemos = new ArrayList<>();

    @Inject
    public SimulatedNetwork() {
        try {
            simulatedWemos.add(
                    new SimulatedWemo(InetAddress.getByName("192.168.6.231"), 49153, "sim/insight1_setup.xml"));
            simulatedWemos.add(
                    new SimulatedWemo(InetAddress.getByName("192.168.6.209"), 49154, "sim/insight2_setup.xml"));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String get(String url) throws IOException {
        return target(url).get(url);
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType) throws IOException {
        return target(url).post(url, headers, payload, contentType);
    }

    private SimulatedWemo target(String url) throws UnknownHostException {
        for (Iterator<SimulatedWemo> iterator = simulatedWemos.iterator(); iterator.hasNext();) {
            SimulatedWemo candidate = iterator.next();
            if (url.startsWith(String.format("http://%s:%d/", candidate.getInetAddress().getHostAddress(), candidate.getPort()))) {
                return candidate;
            }
        }
        throw new UnknownHostException();
    }

    @Override
    public BackgroundProcessHandle startDiscovery(String serviceType, Consumer<SsdpServiceDefinition> serviceConsumer) {
        simulatedWemos.forEach(wemo -> wemo.startDiscovery(serviceType, serviceConsumer));
        return () -> {};
    }
}
