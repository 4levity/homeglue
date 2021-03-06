/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.google.inject.Inject;
import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.DeviceStateProcessorService;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.persistence.PersistenceService;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.stream.Collectors;

@Provider
@Path("/devices")
@Log4j2
@Produces(MediaType.APPLICATION_JSON)
public class DevicesResource {

    private final PersistenceService persistence;
    private final DeviceStateProcessorService stateProcessor;
    private final DeviceResource.Factory deviceResourceFactory;

    @Inject
    public DevicesResource(PersistenceService persistence,
                           DeviceStateProcessorService stateProcessor,
                           DeviceResource.Factory deviceResourceFactory) {
        this.persistence = persistence;
        this.stateProcessor = stateProcessor;
        this.deviceResourceFactory = deviceResourceFactory;
    }

    @GET
    public List<DeviceDto> getDevices(@QueryParam("sort") String sort) {
        return persistence.exec(session -> {
            CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
            CriteriaQuery<Device> query = criteriaBuilder.createQuery(Device.class);
            Root<Device> root = query.from(Device.class);
            String orderBy = sort == null ? Device._detectionId : sort;
            switch (orderBy) {
                case "detectionId":
                case "friendlyName":
                case "connected":
                    query.orderBy(criteriaBuilder.asc(root.get(orderBy)));
                    break;
                default:
                    log.warn("unsupported sort column");
            }
            return session.createQuery(query).list().stream()
                    .map(device -> DeviceDto.from(device, stateProcessor.getLastState(device.getDetectionId())))
                    .collect(Collectors.toList());
        });
    }

    @Path("{detectionId}")
    public DeviceResource device(@PathParam("detectionId") String detectionId) {
        return deviceResourceFactory.create(detectionId);
    }
}
