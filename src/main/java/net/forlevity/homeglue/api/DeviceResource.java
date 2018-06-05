/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.entity.Relay;
import net.forlevity.homeglue.persistence.PersistenceService;
import org.hibernate.Session;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

public class DeviceResource {

    private final PersistenceService persistence;
    private final RelayResource.Factory relayResourceFactory;
    private final ApplianceDetectorResource.Factory applianceDetectorResourceFactory;
    private final String deviceId;

    interface Factory {
        DeviceResource create(String deviceId);
    }

    @Inject
    public DeviceResource(PersistenceService persistence,
                          RelayResource.Factory relayResourceFactory,
                          ApplianceDetectorResource.Factory applianceDetectorResourceFactory,
                          @Assisted String deviceId) {
        this.persistence = persistence;
        this.relayResourceFactory = relayResourceFactory;
        this.applianceDetectorResourceFactory = applianceDetectorResourceFactory;
        this.deviceId = deviceId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDto get() {
        return persistence.exec(session -> DeviceDto.from(getDevice(session, deviceId)));
    }

    @Path("relay")
    public RelayResource relayResource() {
        return relayResourceFactory.create(persistence.exec(session -> getRelay(session, deviceId)));
    }

    @Path("appliance")
    public ApplianceDetectorResource appliaceCfgResource() {
        return applianceDetectorResourceFactory.create(deviceId);
    }

    private Relay getRelay(Session session, String deviceId) {
        Device device = getDevice(session, deviceId);
        Relay relay = device.getRelay();
        if (relay == null) {
            throw new NotFoundException("relay not found");
        }
        return persistence.unproxy(Relay.class, relay);
    }

    static Device getDevice(Session session, String deviceId) {
        Device device = session.bySimpleNaturalId(Device.class).load(deviceId);
        if (device == null) {
            throw new NotFoundException("device not found");
        }
        return device;
    }
}
