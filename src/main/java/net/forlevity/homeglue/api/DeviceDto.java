/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import lombok.*;
import lombok.experimental.Accessors;
import net.forlevity.homeglue.entity.ApplianceDetector;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.entity.Relay;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@ToString
@JsonAutoDetect
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceDto {

    private String deviceId;
    private String friendlyName;
    private Boolean connected;
    private Map<String, String> details;
    private RelayDto relay;
    private ApplianceDetectorDto appliance;

    DeviceDto(String deviceId, Boolean connected, Map<String, String> details) {
        this.deviceId = deviceId;
        this.connected = connected;
        this.details = details;
    }

    public static DeviceDto from(Device device) {
        DeviceDto dto = new DeviceDto(device.getDeviceId(), device.isConnected(), device.getDetails());
        if (!Strings.isNullOrEmpty(device.getFriendlyName())) {
            dto.setFriendlyName(device.getFriendlyName());
        }
        Relay relay = device.getRelay();
        if (relay != null) {
            dto.setRelay(RelayDto.from(relay));
        }
        ApplianceDetector appliance = device.getApplianceDetector();
        if (appliance != null) {
            dto.setAppliance(ApplianceDetectorDto.from(appliance));
        }
        return dto;
    }
}
