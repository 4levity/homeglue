/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LastTelemetryCache implements Consumer<PowerMeterData> {

    public final Map<String, PowerMeterData> lastPowerMeterData = new ConcurrentHashMap<>();

    @Override
    public void accept(PowerMeterData data) {
        if (data.getInstantaneousWatts() != null) {
            this.lastPowerMeterData.put(data.getDeviceId(), data);
        } else {
            this.lastPowerMeterData.remove(data.getDeviceId());
        }
    }
}
