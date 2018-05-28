/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceManager;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The application consists of a Guava ServiceManager and some injected services.
 */
@Log4j2
@Singleton
public class HomeglueApplication {

    @VisibleForTesting
    @Getter(AccessLevel.PACKAGE)
    private final ServiceManager serviceManager;

    @Inject
    public HomeglueApplication(
            PersistenceService persistenceService,
            SsdpDiscoveryService ssdpDiscoveryService,
            Set<DeviceManager> deviceManagers) {
        List<Service> services = new ArrayList<>();
        services.add(persistenceService);
        services.add(ssdpDiscoveryService);
        services.addAll(deviceManagers);
        serviceManager = new ServiceManager(services);
    }

    void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(HomeglueApplication.this::stop));
        serviceManager.startAsync().awaitHealthy();
        String services = serviceManager.servicesByState().values().stream()
                .map(service -> (service.getClass().getSimpleName() + "=" + service.state().toString()))
                .collect(Collectors.joining(", "));
        log.info("application started services: {}", services);
    }

    void stop() {
        System.out.print(" shutting down...");
        serviceManager.stopAsync().awaitStopped();
        System.out.println(" shutdown complete.");
    }
}
