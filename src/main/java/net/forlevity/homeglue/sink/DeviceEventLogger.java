/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sink;

import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceEvent;

import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log4j2
@Singleton
public class DeviceEventLogger implements Consumer<DeviceEvent> {

    @Override
    public void accept(DeviceEvent event) {
        String eventData;
        if (event.getData() != null) {
            eventData = event.getData().entrySet().stream().map(entry -> (entry.getKey() + "=" + entry.getValue()))
                    .collect(Collectors.joining(", "));
        } else {
            eventData = "";
        }
        log.info("Device {}: / Event: {} {}", event.getDeviceId(), event.getEvent(), eventData);
    }
}
