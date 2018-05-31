/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.persistence.PersistenceService;
import net.forlevity.homeglue.util.QueueWorker;
import net.forlevity.homeglue.util.RunnableExecutionThreadService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Log4j2
@Singleton
public class DeviceStatusProcessorService extends RunnableExecutionThreadService implements Consumer<DeviceStatus> {

    private final PersistenceService persistenceService;
    private final Consumer<DeviceEvent> deviceEventConsumer;
    private final QueueWorker<DeviceStatus> deviceStatusWorker;

    @Inject
    public DeviceStatusProcessorService(PersistenceService persistenceService,
                                        Consumer<DeviceEvent> deviceEventConsumer) {
        this.persistenceService = persistenceService;
        this.deviceEventConsumer = deviceEventConsumer;
        deviceStatusWorker = new QueueWorker<>(DeviceStatus.class, this::processStatus);
    }

    @Override
    protected void runUntilInterrupted() {
        deviceStatusWorker.run();
    }

    @Override
    public void accept(DeviceStatus item) {
        deviceStatusWorker.accept(item);
    }

    @VisibleForTesting
    public void processQueue() throws InterruptedException {
        deviceStatusWorker.processQueue();
    }

    /**
     * Process device status. If any changes, generate events and update database.
     * Note this runs serially on queue processing thread.
     *
     * @param newDeviceStatus device status
     */
    private void processStatus(DeviceStatus newDeviceStatus) {
        String deviceId = newDeviceStatus.getDeviceId();
        List<DeviceEvent> newEvents = persistenceService.exec(session -> {
            Device device = session.bySimpleNaturalId(Device.class).load(deviceId);
            List<DeviceEvent> events = new ArrayList<>();
            if (device == null) {
                log.info("device first detection: {}", newDeviceStatus);
                device = Device.from(newDeviceStatus);
                events.add(new DeviceEvent(deviceId, DeviceEvent.NEW_DEVICE, device.getDeviceDetails()));
            } else {
                if (device.isConnected() != newDeviceStatus.isConnected()) {
                    device.setConnected(newDeviceStatus.isConnected());
                    String event = device.isConnected() ? DeviceEvent.CONNECTED : DeviceEvent.CONNECTION_LOST;
                    events.add(new DeviceEvent(deviceId, event));
                }
                if (!device.getDeviceDetails().equals(newDeviceStatus.getDeviceDetails())) {
                    device.setDeviceDetails(newDeviceStatus.getDeviceDetails());
                    events.add(new DeviceEvent(deviceId, DeviceEvent.DETAILS_CHANGED, device.getDeviceDetails()));
                }
            }
            if (events.size() > 0) {
                session.saveOrUpdate(device);
            }
            return events;
        });
        newEvents.forEach(deviceEventConsumer);
    }
}
