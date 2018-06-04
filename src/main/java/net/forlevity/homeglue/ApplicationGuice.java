/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.*;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import lombok.AllArgsConstructor;
import net.forlevity.homeglue.api.ApiGuice;
import net.forlevity.homeglue.device.DeviceEvent;
import net.forlevity.homeglue.device.DeviceManagementGuice;
import net.forlevity.homeglue.device.DeviceStateProcessorService;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.http.SimpleHttpClientImpl;
import net.forlevity.homeglue.ifttt.IftttMakerWebhookClient;
import net.forlevity.homeglue.persistence.H2HibernateService;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.sim.SimulatedNetwork;
import net.forlevity.homeglue.sink.DeviceEventLogger;
import net.forlevity.homeglue.sink.IftttDeviceEventService;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpSearcher;
import net.forlevity.homeglue.upnp.SsdpSearcherImpl;
import net.forlevity.homeglue.util.FanoutExchange;
import net.forlevity.homeglue.util.Json;
import net.forlevity.homeglue.util.ServiceDependencies;
import net.forlevity.homeglue.web.WebserverService;

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

        // JSON processor
        boolean prettyPrintJson = Boolean.valueOf(namedConfigurationProperties.getProperty("json.pretty"));
        Json json = new Json(prettyPrintJson);
        bind(Json.class).toInstance(json);
        bind(ObjectMapper.class).toInstance(json.objectMapper);

        // device events: exchange
        bind(new TypeLiteral<Consumer<DeviceEvent>>(){})
                .to(new TypeLiteral<FanoutExchange<DeviceEvent>>(){}).in(Scopes.SINGLETON);
        // device events: consumers
        Multibinder<Consumer<DeviceEvent>> eventSinkBinder =
                Multibinder.newSetBinder(binder(), new TypeLiteral<Consumer<DeviceEvent>>(){});
        eventSinkBinder.addBinding().to(DeviceEventLogger.class);
        eventSinkBinder.addBinding().to(IftttDeviceEventService.class);

        // upnp
        bind(SsdpDiscoveryService.class);

        // local data storage
        bind(PersistenceService.class).to(H2HibernateService.class);

        // ifttt glue
        bind(IftttMakerWebhookClient.class);
        bind(IftttDeviceEventService.class);

        // use simulated network instead of real devices?
        if (Boolean.valueOf(namedConfigurationProperties.get("network.simulated").toString())) {
            bind(SsdpSearcher.class).to(SimulatedNetwork.class);
            bind(SimpleHttpClient.class).to(SimulatedNetwork.class);
        } else {
            bind(SsdpSearcher.class).to(SsdpSearcherImpl.class);
            bind(SimpleHttpClient.class).to(SimpleHttpClientImpl.class);
        }

        // child modules
        install(new DeviceManagementGuice());
        install(new ApiGuice());
    }

    @Provides
    @Singleton
    public ServiceDependencies serviceDependencies(PersistenceService persistenceService) {
        return new ServiceDependencies(ImmutableMap.of(
                WebserverService.class, ImmutableList.of(persistenceService),
                DeviceStateProcessorService.class, ImmutableList.of(persistenceService)
        ));
    }
}
