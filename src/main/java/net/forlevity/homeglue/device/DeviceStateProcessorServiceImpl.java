/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.entity.ApplianceDetector;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.entity.Relay;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.util.QueueWorkerService;
import net.forlevity.homeglue.util.ServiceDependencies;
import org.hibernate.Session;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Log4j2
@Singleton
public class DeviceStateProcessorServiceImpl extends QueueWorkerService<DeviceState> implements DeviceStateProcessorService {
    private final PersistenceService persistenceService;
    private final ApplianceStateDecider applianceStateDecider;
    private final Consumer<DeviceEvent> deviceEventConsumer;
    private final DeviceCommandDispatcher deviceCommandDispatcher;
    private final Map<Long, Instant> onTooLongSince = new ConcurrentHashMap<>();

    @Inject
    public DeviceStateProcessorServiceImpl(ServiceDependencies dependencies,
                                           PersistenceService persistenceService,
                                           ApplianceStateDecider applianceStateDecider,
                                           Consumer<DeviceEvent> deviceEventConsumer,
                                           DeviceCommandDispatcher deviceCommandDispatcher) {
        super(DeviceState.class, dependencies);
        this.persistenceService = persistenceService;
        this.applianceStateDecider = applianceStateDecider;
        this.deviceEventConsumer = deviceEventConsumer;
        this.deviceCommandDispatcher = deviceCommandDispatcher;
    }

    @Override
    public void handle(DeviceState newDeviceState) {
        String detectionId = newDeviceState.getDetectionId();
        List<DeviceEvent> newEvents = persistenceService.exec(session -> {
            List<DeviceEvent> events = new ArrayList<>();
            Device device = session.bySimpleNaturalId(Device.class).load(detectionId);
            handle(session, device, newDeviceState, events);
            return events;
        });
        newEvents.forEach(deviceEventConsumer);
    }

    private void handle(Session session, Device device, DeviceState newDeviceState, List<DeviceEvent> events) {
        device = handleDeviceConnection(device, newDeviceState, events);
        boolean forceSave = handleRelay(device, newDeviceState, events);
        forceSave |= handleApplianceDetection(device, newDeviceState, events);
        if (events.size() > 0 || forceSave) {
            session.saveOrUpdate(device);
        }
    }

    /**
     * Handle device state change and create new Device entity if needed. Add events to list.
     *
     * @param device device
     * @param newDeviceState new state
     * @param events events (mutable)
     * @return Device entity
     */
    private Device handleDeviceConnection(Device device, DeviceState newDeviceState, List<DeviceEvent> events) {
        // check for new device, connection state changed, details changed
        String detectionId = newDeviceState.getDetectionId();
        if (device == null) {
            log.info("device first detection: {}", newDeviceState);
            device = Device.from(newDeviceState);
            events.add(new DeviceEvent(device, DeviceEvent.NEW_DEVICE, device.getDetails()));
        } else {
            if (device.isConnected() != newDeviceState.isConnected()) {
                device.setConnected(newDeviceState.isConnected());
                String event = device.isConnected() ? DeviceEvent.CONNECTED : DeviceEvent.CONNECTION_LOST;
                events.add(new DeviceEvent(device, event));
            }
            if (!device.getDetails().equals(newDeviceState.getDeviceDetails())) {
                device.setDetails(newDeviceState.getDeviceDetails());
                events.add(new DeviceEvent(device, DeviceEvent.DETAILS_CHANGED, device.getDetails()));
            }
        }
        return device;
    }

    /**
     * Check for and handle relay state changes. Add events to list.
     *
     * @param device device
     * @param newDeviceState new state
     * @param events events (mutable)
     * @return true if no events were added but object changes were made anyhow
     */
    private boolean handleRelay(Device device, DeviceState newDeviceState, List<DeviceEvent> events) {
        // check for relay state change
        boolean forceSave = false;
        if (newDeviceState.getRelayClosed() != null) {
            boolean closed = newDeviceState.getRelayClosed();
            Relay relay = device.getRelay();
            if (relay == null) {
                log.debug("relay discovered on device {}", device.getDetectionId());
                relay = new Relay().setClosed(closed);
                device.setRelay(relay);
                forceSave = true; // no event
            } else {
                if (relay.isClosed() != closed) {
                    relay.setClosed(closed);
                    String event = closed ? DeviceEvent.RELAY_CLOSED : DeviceEvent.RELAY_OPENED;
                    events.add(new DeviceEvent(device, event));
                }
            }
        }
        return forceSave;
    }

    /**
     * Check for and handle appliance state change. Add events to event list.
     *
     * @param device device
     * @param newDeviceState new state
     * @param events events (mutable)
     * @return true if no events were added but object changes were made anyhow
     */
    private boolean handleApplianceDetection(Device device, DeviceState newDeviceState, List<DeviceEvent> events) {
        boolean forceSave = false;
        // appliance detection config
        if (newDeviceState.getInstantaneousWatts() != null) {
            log.info("Read power meter {} {}: {} Watts", device.getDetectionId(), device.getFriendlyName(), newDeviceState.getInstantaneousWatts());
            Double watts = newDeviceState.getInstantaneousWatts();
            ApplianceDetector applianceDetector = device.getApplianceDetector();
            if (applianceDetector == null) {
                log.info("creating default appliance config for meter on device {}", device.getDetectionId());
                applianceDetector = new ApplianceDetector().withDefaultSettings();
                device.setApplianceDetector(applianceDetector);
                boolean initialOnState = applianceStateDecider.applianceOn(applianceDetector, watts);
                applianceDetector.setOn(initialOnState);
                forceSave = true; // no event
            } else {
                boolean currentState = applianceStateDecider.applianceOn(applianceDetector, watts);
                if (applianceDetector.isOn() != currentState) {
                    applianceDetector.setOn(currentState);
                    String event = currentState ? DeviceEvent.APPLIANCE_ON : DeviceEvent.APPLIANCE_OFF;
                    events.add(new DeviceEvent(device, event));
                }
                if (applianceDetector.isOn() && applianceDetector.getMaxOnSeconds() > 0
                        && Duration.between(applianceDetector.getLastStateChange(), Instant.now()).getSeconds()
                        > applianceDetector.getMaxOnSeconds()) {

                    // only create one event when a device is on too long, don't keep creating more events
                    Instant eventSentForStateChange = onTooLongSince.get(device.getId());
                    if (eventSentForStateChange == null
                            || !eventSentForStateChange.equals(applianceDetector.getLastStateChange())) {
                        onTooLongSince.put(device.getId(), applianceDetector.getLastStateChange());
                        events.add(new DeviceEvent(device, DeviceEvent.ON_TOO_LONG));
                    }

                    Relay relay = device.getRelay();
                    if (relay != null) {
                        log.info("Device {} was on too long, opening relay", device.getDetectionId());
                        deviceCommandDispatcher.dispatch(device.getDetectionId(), new Command(Command.Action.OPEN_RELAY));
                    }
                }
            }
        }
        return forceSave;
    }
}
