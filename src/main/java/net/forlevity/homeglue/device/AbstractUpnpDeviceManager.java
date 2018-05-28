/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.sink.DeviceStatus;
import net.forlevity.homeglue.upnp.SsdpDiscoveryService;
import net.forlevity.homeglue.upnp.SsdpServiceDefinition;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Log4j2
public abstract class AbstractUpnpDeviceManager extends AbstractDeviceManager {

    private static final SsdpServiceDefinition POISON = new SsdpServiceDefinition("POISON",null,null,null);

    @VisibleForTesting
    @Getter
    private final LinkedBlockingQueue<SsdpServiceDefinition> discoveredServicesQueue = new LinkedBlockingQueue<>();

    protected AbstractUpnpDeviceManager(Consumer<DeviceStatus> deviceStatusSink,
                                        SsdpDiscoveryService ssdpDiscoveryService,
                                        Predicate<SsdpServiceDefinition> serviceMatcher,
                                        int priority) {
        super(deviceStatusSink);
        ssdpDiscoveryService.registerSsdp(serviceMatcher, this.discoveredServicesQueue::offer, priority);
    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            processDiscoveryQueue();
        }
    }

    /**
     * Block until there are items in the queue, then process queue until empty.
     */
    @VisibleForTesting
    public void processDiscoveryQueue() throws InterruptedException {
        while (processSingleQueueEntry())
            ;
    }

    private boolean processSingleQueueEntry() throws InterruptedException {
        SsdpServiceDefinition entry = discoveredServicesQueue.take();
        if (entry == POISON) {
            return false;
        }
        try {
            processDiscoveredService(entry);
        } catch (RuntimeException e) {
            log.error("unexpected exception processing UPnP service info (continuing)", e);
        }
        return !discoveredServicesQueue.isEmpty();
    }

    /**
     * Subclass defines how to handle when an item is processed on the queue. This will run on the device manager's
     * main thread, one at a time.
     */
    protected abstract void processDiscoveredService(SsdpServiceDefinition service);

    @Override
    protected void triggerShutdown() {
        discoveredServicesQueue.offer(POISON);
    }
}
