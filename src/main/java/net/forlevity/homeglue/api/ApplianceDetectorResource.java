/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.entity.ApplianceDetector;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.persistence.PersistenceService;
import org.hibernate.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Log4j2
@Produces(MediaType.APPLICATION_JSON)
public class ApplianceDetectorResource {

    private final PersistenceService persistence;
    private final String deviceDetectionId;

    interface Factory {
        ApplianceDetectorResource create(String deviceDetectionId);
    }

    @Inject
    public ApplianceDetectorResource(PersistenceService persistence, @Assisted String deviceDetectionId) {
        this.persistence = persistence;
        this.deviceDetectionId = deviceDetectionId;
    }

    @GET
    public ApplianceDetectorDto get() {
        return ApplianceDetectorDto.from(persistence.exec(this::getApplianceDetector));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ApplianceDetectorDto reconfigure(ApplianceDetectorDto newConfig) {
        if (newConfig == null || newConfig.getMinWatts() == null || newConfig.getOffDelaySeconds() == null) {
            throw new BadRequestException("invalid configuration");
        }
        log.info("updating appliance configuration for {} : {}", deviceDetectionId, newConfig);
        return persistence.exec(session -> {
            ApplianceDetector applianceDetector = getApplianceDetector(session);
            applianceDetector.setMinWatts(newConfig.getMinWatts());
            applianceDetector.setOffDelaySecs(newConfig.getOffDelaySeconds());
            applianceDetector.setMaxOnSeconds(newConfig.getMaxOnSeconds());
            session.saveOrUpdate(applianceDetector);
            return ApplianceDetectorDto.from(applianceDetector);
        });
    }

    private ApplianceDetector getApplianceDetector(Session session) {
        Device device = DeviceResource.getDevice(session, deviceDetectionId);
        ApplianceDetector applianceDetector = device.getApplianceDetector();
        if (applianceDetector == null) {
            throw new NotFoundException("appliance detector not found");
        }
        return persistence.unproxy(ApplianceDetector.class, applianceDetector);
    }
}
