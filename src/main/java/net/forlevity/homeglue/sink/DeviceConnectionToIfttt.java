/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sink;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.forlevity.homeglue.ifttt.IftttMakerWebhookClient;

import java.util.function.Consumer;

/**
 * Proof of concept - send device status to IFTTT maker service.
 */
@Singleton
public class DeviceConnectionToIfttt implements Consumer<DeviceStatus> {

    private final IftttMakerWebhookClient webhookClient;

    @Inject
    public DeviceConnectionToIfttt(IftttMakerWebhookClient webhookClient) {
        this.webhookClient = webhookClient;
    }

    @Override
    public void accept(DeviceStatus deviceStatus) {
        webhookClient.trigger("homeglue_device_status",
                deviceStatus.getDeviceId(), Boolean.valueOf(deviceStatus.isConnected()).toString(), null);
    }
}
