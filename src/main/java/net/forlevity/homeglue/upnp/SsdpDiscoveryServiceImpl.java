/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Standard implementation of SsdpDiscoveryService.
 */
@Log4j2
@Singleton
public class SsdpDiscoveryServiceImpl extends AbstractIdleService implements SsdpDiscoveryService {

    private final SsdpSearcher ssdpSearcher;
    private final int ssdpScanPeriodMillis;
    private final int ssdpScanLengthMillis;
    private final int startupDelayMillis;
    private final int minimumInactiveMillis;
    private Instant lastSearchEndTime = Instant.EPOCH;
    private final Object lastSearchLock = new Object();
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
    private final List<Registration> registrations = new ArrayList<>();

    @Inject
    public SsdpDiscoveryServiceImpl(SsdpSearcher ssdpSearcher,
                                    @Named("ssdp.scan.period.millis") int ssdpScanPeriodMillis,
                                    @Named("ssdp.scan.length.millis") int ssdpScanLengthMillis,
                                    @Named("ssdp.startup.delay.millis") int startupDelayMillis,
                                    @Named("ssdp.minimum.inactive.millis") int minimumInactiveMillis) {
        this.ssdpScanPeriodMillis = ssdpScanPeriodMillis;
        this.ssdpScanLengthMillis = ssdpScanLengthMillis;
        this.startupDelayMillis = startupDelayMillis;
        this.minimumInactiveMillis = minimumInactiveMillis;
        this.ssdpSearcher = ssdpSearcher;
    }

    @Override
    public void registerSsdp(Predicate<SsdpServiceDefinition> predicate,
                             Consumer<SsdpServiceDefinition> consumer,
                             int priority) {
        synchronized (registrations) {
            registrations.add(new Registration(priority, predicate, consumer));
            // registration list is sorted in priority order, highest priority (lowest number) first
            Collections.sort(registrations, Comparator.comparingInt(o -> o.priority));
        }
    }

    @Override
    protected void startUp() {
        executor.scheduleAtFixedRate(this::runOnce, startupDelayMillis, ssdpScanPeriodMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void shutDown() {
        executor.shutdownNow();
    }

    /**
     * Execute SSDP discovery requests, but suppress execution if not enough time has passed since last discovery.
     * @throws InterruptedException if interrupted
     */
    @VisibleForTesting
    public void runOnce() {
        log.trace("starting SSDP search");
        synchronized (lastSearchLock) {
            if (lastSearchEndTime.plusMillis(minimumInactiveMillis).isAfter(Instant.now())) {
                log.warn("an SSDP search did not complete on time, ended at {}", lastSearchEndTime);
            } else {
                try {
                    // search for root device since Belkin Wemo Insight does not respond to 'all'
                    search(SsdpSearcher.ROOT_DEVICE_SERVICE_TYPE);
                    // regular search for all services
                    search(null);
                } catch (InterruptedException e) {
                    // if shutDown was called, the executor is terminating this thread
                    log.debug("interrupted during search", e);
                    Thread.currentThread().interrupt();
                }
                lastSearchEndTime = Instant.now();
            }
        }
        log.trace("SSDP search done");
    }

    /**
     * Execute a single SSDP discovery request, handling services as they come in.
     *
     * @param serviceType the service type or null
     * @throws InterruptedException if interrupted
     */
    private void search(String serviceType) throws InterruptedException {
        try (SafeCloseable handle = ssdpSearcher.startDiscovery(serviceType, this::dispatch)) {
            Thread.sleep(ssdpScanLengthMillis); // wait for scan results to come in
        } // autoclose
    }

    /**
     * For a discovered service, look through existing registrations and send matching service descriptions.
     *
     * @param service discovered service
     */
    private void dispatch(SsdpServiceDefinition service) {
        synchronized (registrations) {
            log.debug("found service {} / {} at {}",
                    service.getServiceType(), service.getSerialNumber(), service.getLocation());
            for (int ix = 0; ix < registrations.size(); ix++) {
                Registration registration = registrations.get(ix);
                if (registration.predicate.test(service)) {
                    registration.serviceQueue.accept(service);
                    break;
                }
            }
        }
    }

    @AllArgsConstructor
    private static class Registration {
        public int priority;
        public Predicate<SsdpServiceDefinition> predicate;
        public Consumer<SsdpServiceDefinition> serviceQueue;
    }
}
