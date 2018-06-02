/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sink;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceEvent;
import net.forlevity.homeglue.ifttt.IftttMakerWebhookClient;
import net.forlevity.homeglue.util.QueueWorkerService;

import java.util.Iterator;
import java.util.Map;

/**
 * Send device status updates to IFTTT maker service.
 */
@Log4j2
@Singleton
public class IftttDeviceEventService extends QueueWorkerService<DeviceEvent> {

    private final IftttMakerWebhookClient iftttWebhookClient;

    @Inject
    public IftttDeviceEventService(IftttMakerWebhookClient iftttWebhookClient) {
        super(DeviceEvent.class, null);
        this.iftttWebhookClient = iftttWebhookClient;
    }

    @Override
    protected void handle(DeviceEvent deviceEvent) {
        if (iftttWebhookClient.isEnabled()) {
            IftttMakerWebhookClient.Event iftttEvent = convert(deviceEvent);
            log.info("sending IFTTT webhook: {}", iftttEvent);
            iftttWebhookClient.trigger(iftttEvent);
        }
    }

    private IftttMakerWebhookClient.Event convert(DeviceEvent deviceEvent) {
        String ifttt_event;
        String[] values = new String[3];
        int filled = 0;

        if (deviceEvent.getEvent().equals(DeviceEvent.NEW_DEVICE)) {
            // new device deviceEvent: ifttt deviceEvent code is just "new_device" and first value is new device code
            ifttt_event = deviceEvent.getEvent();
            values[0] = deviceEvent.getDeviceId();
            filled++;
        } else {
            // most events: ifttt deviceEvent code is "device_event"
            ifttt_event = deviceEvent.getDeviceId() + "_" + deviceEvent.getEvent();
        }

        // put max 3 data items in the values fields
        Map<String, String> eventData = deviceEvent.getData();
        if (eventData != null) {
            Iterator<Map.Entry<String, String>> it = eventData.entrySet().iterator();
            while (filled < 3 && it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                values[filled] = entry.getKey() + "=" + entry.getValue();
                filled++;
            }
        }
        return new IftttMakerWebhookClient.Event(ifttt_event, values[0], values[1], values[2]);
    }
}
