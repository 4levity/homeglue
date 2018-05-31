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
import net.forlevity.homeglue.device.DeviceEvent;
import net.forlevity.homeglue.ifttt.IftttMakerWebhookClient;
import net.forlevity.homeglue.util.QueueWorkerThread;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Send device status updates to IFTTT maker service.
 */
@Log4j2
@Singleton
public class IftttDeviceStatusService extends AbstractIdleService implements Consumer<DeviceEvent> {

    private final IftttMakerWebhookClient iftttWebhookClient;
    private final QueueWorkerThread<DeviceEvent> webhookCaller;

    @Inject
    public IftttDeviceStatusService(IftttMakerWebhookClient iftttWebhookClient) {
        this.iftttWebhookClient = iftttWebhookClient;
        webhookCaller = new QueueWorkerThread<>(DeviceEvent.class, this::trigger);
    }

    private void trigger(DeviceEvent event) {
        String ifttt_event;
        String[] values = new String[3];
        int filled = 0;
        if (event.getEvent().equals(DeviceEvent.NEW_DEVICE)) {
            // new device event: ifttt event code is just "new_device" and first value is new device code
            ifttt_event = event.getEvent();
            values[0] = event.getDeviceId();
            filled++;
        } else {
            // most events: ifttt event code is "device_event"
            ifttt_event = event.getDeviceId() + "_" + event.getEvent();
        }

        // put max 3 data items in the values fields
        if (event.getData() != null && !event.getData().isEmpty()) {
            Map<String, String> data = new HashMap<>(event.getData());
            for (int keyx = 1; keyx <= 3; keyx ++) {
                String key = "value" + keyx;
                String valuex = data.get(key);
                if (valuex != null) {
                    values[filled] = valuex;
                    filled++;
                    data.remove(key);
                }
            }
            // if "value1" "value2" "value3" are not specified, just fill some in
            Iterator<Map.Entry<String, String>> it = data.entrySet().iterator();
            while (filled < 3 && it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                values[filled] = entry.getKey() + "=" + entry.getValue();
                filled++;
            }
        }
        log.info("sending IFTTT webhook: {} {} {} {}", ifttt_event, values[0], values[1], values[2]);
        iftttWebhookClient.trigger(
                ifttt_event,
                values[0],
                values[1],
                values[2]);
    }

    /**
     * On receiving an event, queue it to be sent via IFTTT Webhook client.
     *
     * @param event event
     */
    @Override
    public void accept(DeviceEvent event) {
        webhookCaller.accept(event);
    }

    @Override
    protected void startUp() throws Exception {
        webhookCaller.start();
    }

    @Override
    protected void shutDown() throws Exception {
        webhookCaller.interrupt();
    }
}
