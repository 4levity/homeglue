/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceManager;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Log4j2
public class Application {

    private final ServiceManager serviceManager;

    @Inject
    public Application(
            SsdpDiscoveryService ssdpDiscoveryService,
            Set<DeviceManager> deviceManagers) {
        List<Service> services = new ArrayList();
        services.add(ssdpDiscoveryService);
        services.addAll(deviceManagers);
        serviceManager = new ServiceManager(services);
    }

    public void start() {
        serviceManager.startAsync().awaitHealthy();
        String services = serviceManager.servicesByState().values().stream()
                .map(service -> (service.getClass().getSimpleName() + "=" + service.state().toString()))
                .collect(Collectors.joining(", "));
        log.info("application started services: {}", services);
    }
}
