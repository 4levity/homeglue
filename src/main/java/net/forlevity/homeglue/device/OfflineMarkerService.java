/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.persistence.PersistenceService;
import org.hibernate.Session;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log4j2
@Singleton
public class OfflineMarkerService extends AbstractIdleService implements Runnable {

    private static final long INITIAL_DELAY_MILLIS = 60 * 1000; // wait 1 minute before marking anything offline!
    private static final long DELAY_BETWEEN_MILLIS = 30 * 1000; // 30 seconds idle between checks

    private final ScheduledExecutorService executor;
    private final PersistenceService persistence;
    private final DeviceConnectorInstances connectorInstances;
    private final DeviceStateProcessorService stateProcessor;
    private final Consumer<DeviceEvent> deviceEventConsumer;
    private ScheduledFuture<?> checker;

    @Inject
    public OfflineMarkerService(ScheduledExecutorService executor,
                                PersistenceService persistence,
                                DeviceConnectorInstances connectorInstances,
                                DeviceStateProcessorService stateProcessor,
                                Consumer<DeviceEvent> deviceEventConsumer) {
        this.executor = executor;
        this.persistence = persistence;
        this.connectorInstances = connectorInstances;
        this.stateProcessor = stateProcessor;
        this.deviceEventConsumer = deviceEventConsumer;
    }

    @Override
    protected void startUp() throws Exception {
        persistence.awaitRunning();
        stateProcessor.awaitRunning();
        checker = executor.scheduleWithFixedDelay(this, INITIAL_DELAY_MILLIS, DELAY_BETWEEN_MILLIS, TimeUnit.MILLISECONDS);
    }

    @Override
    synchronized public void run() {
        try {
            List<Device> marked = persistence.exec(this::markOffline);
            if (marked.size() > 0) {
                String devices = marked.stream().map(Device::getDetectionId)
                        .collect(Collectors.joining(", "));
                log.info("marked {} devices offline: {}", marked.size(), devices);
                marked.forEach(device -> {
                    deviceEventConsumer.accept(new DeviceEvent(device, DeviceEvent.CONNECTION_LOST));
                });
            }
        } catch (RuntimeException e) {
            log.error("unexpected exception during offline marker (continuing)", e);
        }
    }

    /**
     * Manually mark a device offline without waiting for the periodic checker. Connector/manager may call this if a
     * device has gone offline, but should try to avoid calling this method for very brief connection interruptions.
     *
     * @param detectionId device detectionId
     * @return true if the device was successfully marked offline (valid detectionId)
     */
    synchronized public boolean markOffline(String detectionId) {
        return persistence.exec(session -> {
            Device device = session.bySimpleNaturalId(Device.class).load(detectionId);
            if (device == null) {
                log.warn("can't mark unknown device offline: {}", detectionId);
            } else if (!device.isConnected()) {
                log.debug("device was already marked offline: {}", detectionId);
            } else {
                device.setConnected(false);
                session.saveOrUpdate(device);
            }
            return device != null;
        });
    }

    private List<Device> markOffline(Session session) {

        // query: all devices where connected = true
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<Device> query = criteriaBuilder.createQuery(Device.class);
        Root<Device> root = query.from(Device.class);
        query.where(criteriaBuilder.equal(root.get(Device._connected), true));
        List<Device> onlineDevices = session.createQuery(query).list();

        // call checkOffline on each device, return non-null results as list
        return onlineDevices.stream().map(dev -> checkOffline(session, dev))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Mark a device offline, if it should be marked offline.
     *
     * @param device device
     * @return device detection id, if marked offline, or null if nothing happened
     */
    private Device checkOffline(Session session, Device device) {
        String id = device.getDetectionId();
        DeviceState lastState = stateProcessor.getLastState(id);
        DeviceConnectorInfo connectorInfo = connectorInstances.get(id);
        if (lastState == null || connectorInfo == null
                || lastState.getTimestamp().plus(connectorInfo.getOfflineDelay()).isBefore(Instant.now())) {
            device.setConnected(false);
            session.saveOrUpdate(device);
            return device;
        } // else
        return null;
    }

    @Override
    protected void shutDown() throws Exception {
        if (checker != null) {
            checker.cancel(true);
        }
    }
}
