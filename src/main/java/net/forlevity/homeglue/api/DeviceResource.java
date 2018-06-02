/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.persistence.PersistenceService;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class DeviceResource {

    private final PersistenceService persistence;
    private final String deviceId;

    @Inject
    public DeviceResource(PersistenceService persistence, @Assisted String deviceId) {
        this.persistence = persistence;
        this.deviceId = deviceId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDto get() {
        return persistence.exec(session -> {
            Device device = session.bySimpleNaturalId(Device.class).load(deviceId);
            if (device == null) {
                throw new NotFoundException("device not found");
            }
            return DeviceDto.from(device);
        });
    }

    interface Factory {
        DeviceResource create(String deviceId);
    }
}
