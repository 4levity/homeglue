/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import com.google.inject.Singleton;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Singleton
@Log4j2
public class DeviceCommandDispatcher {

    private final Map<String, DeviceConnector> devices = new ConcurrentHashMap<>();

    public void register(DeviceConnector deviceConnectorInstance) {
        devices.put(deviceConnectorInstance.getDetectionId(), deviceConnectorInstance);
    }

    public Future<Command.Result> dispatch(String deviceDetectionId, Command command) {
        DeviceConnector device = devices.get(deviceDetectionId);
        if (device == null) {
            return CompletableFuture.completedFuture(Command.Result.DEVICE_NOT_FOUND);
        } // else
        log.info("command for device {} : {}", deviceDetectionId, command);
        return device.dispatch(command);
    }
}
