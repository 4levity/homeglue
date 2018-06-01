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
import net.forlevity.homeglue.device.DeviceStateProcessorService;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.sink.IftttDeviceEventService;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import org.apache.logging.log4j.LogManager;

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

    @VisibleForTesting
    @Getter
    private volatile boolean stopped = false;

    @Inject
    public HomeglueApplication(
            PersistenceService persistenceService,
            DeviceStateProcessorService deviceStateProcessorService,
            SsdpDiscoveryService ssdpDiscoveryService,
            IftttDeviceEventService iftttDeviceEventService,
            Set<Service> deviceManagers) {
        List<Service> services = new ArrayList<>();
        services.add(persistenceService);
        services.add(deviceStateProcessorService);
        services.add(iftttDeviceEventService);
        services.add(ssdpDiscoveryService);
        services.addAll(deviceManagers);
        serviceManager = new ServiceManager(services);
    }

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(HomeglueApplication.this::shutdownHook));
        serviceManager.startAsync().awaitHealthy();
        String services = serviceManager.servicesByState().values().stream()
                .map(service -> (service.getClass().getSimpleName() + "=" + service.state().toString()))
                .collect(Collectors.joining(", "));
        log.info("application started services: {}", services);
    }

    public void stop() {
        if (!stopped) {
            stopped = true;
            System.out.print(" shutting down...");
            serviceManager.stopAsync().awaitStopped();
            System.out.println(" shutdown complete.");
        }
    }

    private void shutdownHook() {
        stop();
        // log4j2.yaml disables log4j shutdown hook, so that we can shut it down manually after everything's done
        LogManager.shutdown();
    }
}
