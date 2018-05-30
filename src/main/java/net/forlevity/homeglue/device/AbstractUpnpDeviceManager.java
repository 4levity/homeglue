/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.sink.DeviceStatusChange;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;
import net.forlevity.homeglue.util.QueueProcessingThread;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Log4j2
public abstract class AbstractUpnpDeviceManager extends AbstractDeviceManager
        implements Consumer<SsdpServiceDefinition> {

    @VisibleForTesting
    @Getter
    private final QueueProcessingThread<SsdpServiceDefinition> discoveryProcessor =
            new QueueProcessingThread<>(SsdpServiceDefinition.class, this);

    protected AbstractUpnpDeviceManager(PersistenceService persistenceService,
                                        Consumer<DeviceStatusChange> deviceStatusChangeSink,
                                        SsdpDiscoveryService ssdpDiscoveryService,
                                        Predicate<SsdpServiceDefinition> serviceMatcher,
                                        int priority) {
        super(persistenceService, deviceStatusChangeSink);
        ssdpDiscoveryService.registerSsdp(serviceMatcher, discoveryProcessor::accept, priority);
    }

    @Override
    protected void startUp() throws Exception {
        super.startUp();
        discoveryProcessor.start();
    }

    @Override
    protected void shutDown() throws Exception {
        discoveryProcessor.interrupt();
        super.shutDown();
    }
}
