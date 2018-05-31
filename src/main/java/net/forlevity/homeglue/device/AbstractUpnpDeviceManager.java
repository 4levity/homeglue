/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;
import net.forlevity.homeglue.util.QueueWorker;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Log4j2
public abstract class AbstractUpnpDeviceManager extends AbstractDeviceManager
        implements Consumer<SsdpServiceDefinition> {

    private final QueueWorker<SsdpServiceDefinition> discoveryProcessor;

    protected AbstractUpnpDeviceManager(Consumer<DeviceStatus> deviceStatusSink,
                                        SsdpDiscoveryService ssdpDiscoveryService,
                                        Predicate<SsdpServiceDefinition> serviceMatcher,
                                        int priority) {
        super(deviceStatusSink);
        discoveryProcessor = new QueueWorker<>(SsdpServiceDefinition.class, this::notifyServiceDiscovered);
        ssdpDiscoveryService.registerSsdp(serviceMatcher, discoveryProcessor::accept, priority);
    }

    @Override
    protected void runUntilInterrupted() {
        discoveryProcessor.run();
    }

    @Override
    public void accept(SsdpServiceDefinition item) {
        discoveryProcessor.accept(item);
    }

    @VisibleForTesting
    public void processQueue() throws InterruptedException {
        discoveryProcessor.processQueue();
    }

    @VisibleForTesting
    public BlockingQueue<SsdpServiceDefinition> getQueue() {
        return discoveryProcessor.getQueue();
    }

    /**
     * Subclass implements this to process incoming service definitions.
     * May be called many times for the same service.
     *
     * @param serviceDefinition service
     */
    protected abstract void notifyServiceDiscovered(SsdpServiceDefinition serviceDefinition);
}
