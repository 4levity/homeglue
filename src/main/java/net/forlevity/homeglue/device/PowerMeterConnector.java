package net.forlevity.homeglue.device;

public interface PowerMeterConnector extends DeviceConnector {

    PowerMeterData read();
}
