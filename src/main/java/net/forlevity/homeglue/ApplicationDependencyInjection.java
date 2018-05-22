/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import net.forlevity.homeglue.device.DeviceManagementDependencyInjection;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.http.SimpleHttpClientImpl;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.storage.NoStorage;
import net.forlevity.homeglue.storage.TelemetrySink;
import net.forlevity.homeglue.upnp.SsdpSearcher;
import net.forlevity.homeglue.upnp.SsdpSearcherImpl;

import java.io.IOException;
import java.util.Properties;

/**
 * Top level dependency injection module.
 */
public class ApplicationDependencyInjection extends AbstractModule {

    @Override
    protected void configure() {
        // the application
        bind(HomeglueApplication.class);

        // configuration
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/default.homeglue.properties"));
        } catch (IOException e) {
            throw new RuntimeException("internal error loading default properties", e);
        }
        Names.bindProperties(binder(), properties);

        // use simulation instead of real devices?
        if (Boolean.valueOf(properties.get("network.simulated").toString())) {
            bind(SsdpSearcher.class).to(SimulatedNetwork.class);
            bind(SimpleHttpClient.class).to(SimulatedNetwork.class);
        } else {
            bind(SsdpSearcher.class).to(SsdpSearcherImpl.class);
            bind(SimpleHttpClient.class).to(SimpleHttpClientImpl.class);
        }

        // device manager child module
        install(new DeviceManagementDependencyInjection());

        // storage
        bind(DeviceStatusSink.class).to(NoStorage.class);
        bind(TelemetrySink.class).to(NoStorage.class);
    }
}
