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
import net.forlevity.homeglue.device.DeviceStatusChange;
import net.forlevity.homeglue.ifttt.IftttMakerWebhookClient;
import net.forlevity.homeglue.util.QueueWorkerThread;

import java.util.function.Consumer;

/**
 * Send device status updates to IFTTT maker service.
 */
@Log4j2
@Singleton
public class IftttDeviceStatusService extends AbstractIdleService implements Consumer<DeviceStatusChange> {

    private static final String DEVICE_STATUS_EVENT = "homeglue_device_status";

    private final IftttMakerWebhookClient webhookClient;
    private final QueueWorkerThread<DeviceStatusChange> senderThread;

    @Inject
    public IftttDeviceStatusService(IftttMakerWebhookClient webhookClient) {
        this.webhookClient = webhookClient;
        senderThread = new QueueWorkerThread<>(DeviceStatusChange.class, this::trigger);
    }

    private void trigger(DeviceStatusChange newStatus) {
        webhookClient.trigger(
                DEVICE_STATUS_EVENT,
                newStatus.getDeviceId(),
                Boolean.valueOf(newStatus.isConnected()).toString(),
                null);
    }

    /**
     * On receiving a status change, queue it to be sent via IFTTT Webhook client.
     *
     * @param newStatus
     */
    @Override
    public void accept(DeviceStatusChange newStatus) {
        senderThread.accept(newStatus);
    }

    @Override
    protected void startUp() throws Exception {
        senderThread.start();
    }

    @Override
    protected void shutDown() throws Exception {
        senderThread.interrupt();
    }
}
