package net.forlevity.homeglue.device;

import java.util.Map;

public interface DeviceConnector {

    String DEVICE_ID_UNKNOWN = "unknown";

    boolean connect();

    boolean isConnected();

    String getDeviceId();

    Map<String,String> getDeviceDetails();
}
