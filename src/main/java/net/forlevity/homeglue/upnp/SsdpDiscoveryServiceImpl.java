/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.resourcepool.ssdp.model.DiscoveryRequest;
import io.resourcepool.ssdp.model.SsdpService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@Log4j2
@Singleton
public class SsdpDiscoveryServiceImpl extends AbstractIdleService implements SsdpDiscoveryService {

    private static final long SSDP_SEARCH_PERIOD_MILLIS = 30 * 1000;
    private static final long SSDP_SEARCH_ACTIVE_MILLIS = 8 * 1000;
    private static final long STARTUP_DELAY_MILLIS = 250;
    private static final Duration MINIMUM_TIME_BETWEEN_SEARCHES = Duration.ofSeconds(5);

    private final SsdpSearcher ssdpSearcher;
    private Instant lastSearchEndTime = Instant.now().minus(MINIMUM_TIME_BETWEEN_SEARCHES);
    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);
    private final List<Registration> registrations = new ArrayList<>();

    @Inject
    public SsdpDiscoveryServiceImpl(SsdpSearcher ssdpSearcher) {
        this.ssdpSearcher = ssdpSearcher;
    }

    @Override
    public void registerSsdp(Predicate<SsdpService> predicate, Queue<SsdpService> serviceQueue, int priority) {
        synchronized (registrations) {
            registrations.add(new Registration(priority, predicate, serviceQueue));
            Collections.sort(registrations, Comparator.comparingInt(o -> o.priority));
        }
    }

    @Override
    protected void startUp() throws Exception {
        executor.scheduleAtFixedRate(() -> {
            try {
                log.trace("starting search");
                search();
                log.trace("search done");
            } catch (InterruptedException e) {
                log.warn("interrupted during search", e);
            }
        }, STARTUP_DELAY_MILLIS, SSDP_SEARCH_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void shutDown() throws Exception {
        executor.shutdown();
    }

    private void search() throws InterruptedException {
        synchronized (lastSearchEndTime) {
            if (lastSearchEndTime != null
                    && lastSearchEndTime.plus(MINIMUM_TIME_BETWEEN_SEARCHES).isAfter(Instant.now())) {
                log.warn("an SSDP search did not complete on time, ended at {}", lastSearchEndTime);
            } else {
                // search for root device since Belkin Wemo Insight does not respond to 'all'
                search(DiscoveryRequest.discoverRootDevice());
                search(DiscoveryRequest.discoverAll());
                lastSearchEndTime = Instant.now();
            }
        }
    }

    private void search(DiscoveryRequest discoveryRequest) throws InterruptedException {
        BackgroundProcess discovery = null;
        try {
            discovery = ssdpSearcher.startDiscovery(discoveryRequest, service -> dispatch(service));
            Thread.sleep(SSDP_SEARCH_ACTIVE_MILLIS);
        } finally {
            if (discovery != null) {
                discovery.stop();
            }
        }
    }

    private void dispatch(SsdpService service) {
        synchronized (registrations) {
            log.debug("found service {} / {} at {}",
                    service.getServiceType(), service.getSerialNumber(), service.getLocation());
            for (int ix = 0; ix < registrations.size(); ix++) {
                Registration registration = registrations.get(ix);
                if (registration.predicate.test(service)) {
                    registration.serviceQueue.offer(service);
                    return;
                }
            }
        }
    }

    @AllArgsConstructor
    private static class Registration {
        public int priority;
        public Predicate<SsdpService> predicate;
        public Queue<SsdpService> serviceQueue;
    }
}
