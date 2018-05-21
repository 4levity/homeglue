package net.forlevity.homeglue;

import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceManager;

import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@Log4j2
public class Application {

    private final ServiceManager deviceManagerServiceManager;

    @Inject
    public Application(Set<DeviceManager> deviceManagers) {
        deviceManagerServiceManager = new ServiceManager(deviceManagers);
    }

    public void start() {
        deviceManagerServiceManager.startAsync().awaitHealthy();
        String services = deviceManagerServiceManager.servicesByState().values().stream()
                .map(service -> (service.getClass().getSimpleName() + "=" + service.state().toString()))
                .collect(Collectors.joining(", "));
        log.info("application started services: {}", services);
    }
}
