package net.forlevity.homeglue.storage;

import net.forlevity.homeglue.device.PowerMeterData;

public interface TelemetrySink {

    void accept(String deviceId, PowerMeterData data);
}
