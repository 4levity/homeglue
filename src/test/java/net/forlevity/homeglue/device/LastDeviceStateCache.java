/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LastDeviceStateCache implements Consumer<DeviceState> {

    public final Map<String, DeviceState> lastDeviceState = new ConcurrentHashMap<>();

    @Override
    public void accept(DeviceState data) {
        this.lastDeviceState.put(data.getDetectionId(), data);
    }
}
