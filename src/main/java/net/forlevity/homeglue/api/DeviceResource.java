/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import net.forlevity.homeglue.device.DeviceStateProcessorService;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.entity.Relay;
import net.forlevity.homeglue.persistence.PersistenceService;
import org.hibernate.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
public class DeviceResource {

    private final PersistenceService persistence;
    private final DeviceStateProcessorService stateProcessor;
    private final RelayResource.Factory relayResourceFactory;
    private final ApplianceDetectorResource.Factory applianceDetectorResourceFactory;
    private final String detectionId;

    interface Factory {
        DeviceResource create(String detectionId);
    }

    @Inject
    public DeviceResource(PersistenceService persistence,
                          DeviceStateProcessorService stateProcessor,
                          RelayResource.Factory relayResourceFactory,
                          ApplianceDetectorResource.Factory applianceDetectorResourceFactory,
                          @Assisted String detectionId) {
        this.persistence = persistence;
        this.stateProcessor = stateProcessor;
        this.relayResourceFactory = relayResourceFactory;
        this.applianceDetectorResourceFactory = applianceDetectorResourceFactory;
        this.detectionId = detectionId;
    }

    @GET
    public DeviceDto get() {
        return persistence.exec(session ->
                DeviceDto.from(getDevice(session, detectionId), stateProcessor.getLastState(detectionId)));
    }

    /**
     * Set friendlyName of a device.
     *
     * @param dto dto
     * @return device
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public DeviceDto post(DeviceDto dto) {
        if (dto.getFriendlyName() == null) {
            throw new BadRequestException("no name specified");
        }
        return persistence.exec(session -> {
            Device device = getDevice(session, detectionId);
            if (dto.getFriendlyName().equals("")) {
                device.setFriendlyName(null);
            } else {
                device.setFriendlyName(dto.getFriendlyName());
            }
            session.saveOrUpdate(device);
            return DeviceDto.from(device, stateProcessor.getLastState(detectionId));
        });
    }

    @Path("relay")
    public RelayResource relayResource() {
        return relayResourceFactory.create(persistence.exec(session -> getRelay(session, detectionId)));
    }

    @Path("appliance")
    public ApplianceDetectorResource applianceResource() {
        return applianceDetectorResourceFactory.create(detectionId);
    }

    private Relay getRelay(Session session, String deviceDetectionId) {
        Device device = getDevice(session, deviceDetectionId);
        Relay relay = device.getRelay();
        if (relay == null) {
            throw new NotFoundException("relay not found");
        }
        return persistence.unproxy(Relay.class, relay);
    }

    static Device getDevice(Session session, String detectionId) {
        Device device = session.bySimpleNaturalId(Device.class).load(detectionId);
        if (device == null) {
            throw new NotFoundException("device not found");
        }
        return device;
    }
}
