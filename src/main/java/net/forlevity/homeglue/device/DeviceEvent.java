/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import lombok.*;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
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

    @NonNull
    private String deviceId;
    @NonNull
    private String event;

    private Map<String, String> data = null;
}
