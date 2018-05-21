package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@ToString(of = {"deviceId"})
public abstract class AbstractDeviceConnector implements DeviceConnector {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String deviceId = DEVICE_ID_UNKNOWN;

    private Map<String, String> deviceDetails = new HashMap<>();

    public Map<String, String> getDeviceDetails() {
        return ImmutableMap.copyOf(deviceDetails);
    }

    protected void setDeviceDetail(String key, String value) {
        if (value != null) {
            deviceDetails.put(key, value);
        } else {
            deviceDetails.remove(key);
        }
    }
}
