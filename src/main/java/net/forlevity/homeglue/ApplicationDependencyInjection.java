/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.AllArgsConstructor;
import net.forlevity.homeglue.device.DeviceManagementDependencyInjection;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.http.SimpleHttpClientImpl;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.storage.DeviceStatusSink;
import net.forlevity.homeglue.storage.NoStorage;
import net.forlevity.homeglue.storage.TelemetrySink;
import net.forlevity.homeglue.upnp.SsdpDiscoveryServiceImpl;
import net.forlevity.homeglue.upnp.SsdpSearcher;
import net.forlevity.homeglue.upnp.SsdpSearcherImpl;

import java.util.Properties;

/**
 * Top level dependency injection module. Caller must pass in a complete configuration with all required @Named values.
 */
@AllArgsConstructor
public class ApplicationDependencyInjection extends AbstractModule {

    private Properties namedConfigurationProperties;

    @Override
    protected void configure() {

        // configuration
        Names.bindProperties(binder(), namedConfigurationProperties);

        // the application
        bind(HomeglueApplication.class);

        // storage
        bind(DeviceStatusSink.class).to(NoStorage.class);
        bind(TelemetrySink.class).to(NoStorage.class);

        // http
        bind(SimpleHttpClient.class).to(SimpleHttpClientImpl.class);

        // upnp
        bind(SsdpSearcher.class).to(SsdpSearcherImpl.class);
        bind(SsdpDiscoveryServiceImpl.class).to(SsdpDiscoveryServiceImpl.class);

        // use simulation instead of real devices?
        if (Boolean.valueOf(namedConfigurationProperties.get("network.simulated").toString())) {
            bind(SsdpSearcher.class).to(SimulatedNetwork.class);
            bind(SimpleHttpClient.class).to(SimulatedNetwork.class);
        } else {
            bind(SsdpSearcher.class).to(SsdpSearcherImpl.class);
            bind(SimpleHttpClient.class).to(SimpleHttpClientImpl.class);
        }

        // device manager child module
        install(new DeviceManagementDependencyInjection());
    }
}
