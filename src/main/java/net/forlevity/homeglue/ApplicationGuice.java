/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import lombok.AllArgsConstructor;
import net.forlevity.homeglue.device.*;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.http.SimpleHttpClientImpl;
import net.forlevity.homeglue.ifttt.IftttMakerWebhookClient;
import net.forlevity.homeglue.ifttt.IftttMakerWebhookClientImpl;
import net.forlevity.homeglue.persistence.H2HibernateService;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.sink.DeviceEventLogger;
import net.forlevity.homeglue.sink.IftttDeviceEventService;
import net.forlevity.homeglue.sink.TelemetryLogger;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpSearcher;
import net.forlevity.homeglue.upnp.SsdpSearcherImpl;
import net.forlevity.homeglue.util.FanoutExchange;

import java.util.Properties;
import java.util.function.Consumer;

/**
 * Top level dependency injection module. Caller must pass in a complete configuration with all required @Named values.
 */
@AllArgsConstructor
public class ApplicationGuice extends AbstractModule {

    private Properties namedConfigurationProperties;

    @Override
    protected void configure() {

        // configuration
        Names.bindProperties(binder(), namedConfigurationProperties);

        // the application
        bind(HomeglueApplication.class);

        // device status processor
        bind(new TypeLiteral<Consumer<DeviceStatus>>(){}).to(DeviceStatusProcessorService.class);

        // device events: exchange
        bind(new TypeLiteral<Consumer<DeviceEvent>>(){})
                .to(new TypeLiteral<FanoutExchange<DeviceEvent>>(){}).in(Scopes.SINGLETON);
        // device events: consumers
        Multibinder<Consumer<DeviceEvent>> eventSinkBinder =
                Multibinder.newSetBinder(binder(), new TypeLiteral<Consumer<DeviceEvent>>(){});
        eventSinkBinder.addBinding().to(DeviceEventLogger.class);
        eventSinkBinder.addBinding().to(IftttDeviceEventService.class);

        // telemetry: exchange
        bind(new TypeLiteral<Consumer<PowerMeterData>>(){})
                .to(new TypeLiteral<FanoutExchange<PowerMeterData>>(){}).in(Scopes.SINGLETON);
        // telemetry: consumers
        Multibinder<Consumer<PowerMeterData>> telemetrySinkBinder =
                Multibinder.newSetBinder(binder(), new TypeLiteral<Consumer<PowerMeterData>>(){});
        telemetrySinkBinder.addBinding().to(TelemetryLogger.class);

        // upnp
        bind(SsdpDiscoveryService.class);

        // local data storage
        bind(PersistenceService.class).to(H2HibernateService.class);

        // ifttt
        bind(IftttMakerWebhookClient.class).to(IftttMakerWebhookClientImpl.class);

        // use simulation instead of real devices?
        if (Boolean.valueOf(namedConfigurationProperties.get("network.simulated").toString())) {
            bind(SsdpSearcher.class).to(SimulatedNetwork.class);
            bind(SimpleHttpClient.class).to(SimulatedNetwork.class);
        } else {
            bind(SsdpSearcher.class).to(SsdpSearcherImpl.class);
            bind(SimpleHttpClient.class).to(SimpleHttpClientImpl.class);
        }

        // device manager child module
        install(new DeviceManagementGuice());
    }
}
