package net.forlevity.homeglue.storage;

import net.forlevity.homeglue.device.DeviceConnector;

public interface DeviceStatusSink {

    void accept(DeviceConnector deviceConnector);
}
