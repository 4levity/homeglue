package net.forlevity.homeglue.device;

import java.util.Map;

public interface DeviceConnector {

    boolean connect();

    boolean isConnected();

    String getDeviceId();

    Map<String,String> getDeviceDetails();
}
