/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sink;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.ifttt.IftttMakerWebhookClient;
import net.forlevity.homeglue.util.QueueProcessingThread;

import java.util.function.Consumer;

/**
 * Send device status updates to IFTTT maker service.
 */
@Log4j2
@Singleton
public class IftttDeviceStatusService extends AbstractIdleService implements Consumer<DeviceStatus> {

    private static final String DEVICE_STATUS_EVENT = "homeglue_device_status";

    private final IftttMakerWebhookClient webhookClient;
    private final QueueProcessingThread<DeviceStatus> deviceStatusProcessor;

    @Inject
    public IftttDeviceStatusService(IftttMakerWebhookClient webhookClient) {
        this.webhookClient = webhookClient;
        deviceStatusProcessor = new QueueProcessingThread<>(DeviceStatus.class, this::trigger);
    }

    private void trigger(DeviceStatus deviceStatus) {
        webhookClient.trigger(
                DEVICE_STATUS_EVENT,
                deviceStatus.getDeviceId(),
                Boolean.valueOf(deviceStatus.isConnected()).toString(),
                null);
    }

    @Override
    public void accept(DeviceStatus deviceStatus) {
        deviceStatusProcessor.accept(deviceStatus);
    }

    @Override
    protected void startUp() throws Exception {
        deviceStatusProcessor.start();
    }

    @Override
    protected void shutDown() throws Exception {
        deviceStatusProcessor.interrupt();
    }
}
