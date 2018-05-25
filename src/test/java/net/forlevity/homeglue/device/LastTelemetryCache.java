/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import net.forlevity.homeglue.storage.NoStorage;
import net.forlevity.homeglue.storage.PowerMeterData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LastTelemetryCache extends NoStorage {

    public final Map<String, PowerMeterData> lastPowerMeterData = new ConcurrentHashMap<>();

    @Override
    public void accept(String deviceId, PowerMeterData data) {
        if (data != null) {
            this.lastPowerMeterData.put(deviceId, data);
        } else {
            this.lastPowerMeterData.remove(deviceId);
        }
        super.accept(deviceId, data);
    }
}
