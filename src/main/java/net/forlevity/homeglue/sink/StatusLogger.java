/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sink;

import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Log4j2
public class StatusLogger implements Consumer<DeviceStatus> {

    @Override
    public void accept(DeviceStatus deviceConnector) {
        String deviceId = deviceConnector.getDeviceId();
        Map<String, String> deviceDetails = deviceConnector.getDeviceDetails();
        log.info("device {} : connected={}, {}", deviceId, deviceConnector.isConnected(),
                deviceDetails.entrySet().stream().map(entry -> (entry.getKey() + "=" + entry.getValue()))
                        .collect(Collectors.joining(", ")));
    }
}
