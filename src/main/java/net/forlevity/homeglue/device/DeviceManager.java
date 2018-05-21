package net.forlevity.homeglue.device;

import com.google.common.util.concurrent.Service;

import java.util.Collection;

public interface DeviceManager extends Service {

    Collection<DeviceConnector> getDevices();
}
