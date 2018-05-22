/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.storage;

import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceConnector;
import net.forlevity.homeglue.device.PowerMeterData;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation for device and telemetry data that just logs and discards the data.
 */
@Log4j2
@Singleton
public class NoStorage implements DeviceStatusSink, TelemetrySink {

    @Override
    public void accept(String deviceId, PowerMeterData data) {
        log.info("device {} : {}", deviceId, data);
    }

    @Override
    public void accept(DeviceConnector deviceConnector) {
        String deviceId = deviceConnector.getDeviceId();
        Map<String, String> deviceDetails = deviceConnector.getDeviceDetails();
        log.info("device {} : connected={}, {}", deviceId, deviceConnector.isConnected(),
                deviceDetails.entrySet().stream().map(entry -> (entry.getKey() + "=" + entry.getValue()))
                        .collect(Collectors.joining(", ")));
    }
}
