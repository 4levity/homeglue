/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.upnp;

import io.resourcepool.ssdp.model.SsdpService;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.net.InetAddress;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SsdpServiceDefinition {

    private final String serialNumber;
    private final String serviceType;
    private final String location;
    private final InetAddress remoteIp;

    public SsdpServiceDefinition(SsdpService ssdpService) {
        this.serialNumber = ssdpService.getSerialNumber();
        this.serviceType = ssdpService.getServiceType();
        this.location = ssdpService.getLocation();
        this.remoteIp = ssdpService.getRemoteIp();
    }
}
