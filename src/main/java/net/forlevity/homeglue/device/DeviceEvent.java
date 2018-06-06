/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import net.forlevity.homeglue.entity.Device;

import java.util.Map;

@NoArgsConstructor
@Getter
@ToString
public class DeviceEvent {

    // some built-in events
    public static final String CONNECTED = "connected";
    public static final String CONNECTION_LOST = "connection_lost";
    public static final String RELAY_CLOSED = "relay_closed";
    public static final String RELAY_OPENED = "relay_open";
    public static final String APPLIANCE_ON = "appliance_on";
    public static final String APPLIANCE_OFF = "appliance_off";
    public static final String DETAILS_CHANGED = "details_changed";
    public static final String NEW_DEVICE = "new_device";
    public static final String ON_TOO_LONG = "on_too_long";

    @NonNull
    private String detectionId;
    private String friendlyName;
    @NonNull
    private String event;

    private Map<String, String> data = null;

    public DeviceEvent(Device device, String event) {
        this.detectionId = device.getDetectionId();
        this.friendlyName = device.getFriendlyName();
        this.event = event;
    }

    public DeviceEvent(Device device, String event, Map<String, String> data) {
        this.detectionId = device.getDetectionId();
        this.friendlyName = device.getFriendlyName();
        this.event = event;
        this.data = data;
    }
}
