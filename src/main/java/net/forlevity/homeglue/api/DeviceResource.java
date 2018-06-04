/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import net.forlevity.homeglue.device.Command;
import net.forlevity.homeglue.device.DeviceCommandDispatcher;
import net.forlevity.homeglue.entity.Device;
import net.forlevity.homeglue.entity.Relay;
import net.forlevity.homeglue.persistence.PersistenceService;
import org.hibernate.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeviceResource {

    private final PersistenceService persistence;
    private final String deviceId;
    private final DeviceCommandDispatcher commandDispatcher;

    interface Factory {
        DeviceResource create(String deviceId);
    }

    @Inject
    public DeviceResource(PersistenceService persistence,
                          DeviceCommandDispatcher commandDispatcher,
                          @Assisted String deviceId) {
        this.persistence = persistence;
        this.commandDispatcher = commandDispatcher;
        this.deviceId = deviceId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DeviceDto get() {
        return persistence.exec(session -> DeviceDto.from(getDevice(session, deviceId)));
    }

    @GET
    @Path("relay")
    @Produces(MediaType.APPLICATION_JSON)
    public boolean getRelayState() {
        return persistence.exec(session -> getRelay(session, deviceId).isClosed());
    }

    @POST
    @Path("relay")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Command.Result setRelayState(Boolean newState) throws ExecutionException, InterruptedException {
        if (newState == null) {
            throw new BadRequestException("relay state must be true (closed) or false (open)");
        }
        Future<Command.Result> result = persistence.exec(session -> {
            getRelay(session, deviceId); // throw if not
            Command.Action action = newState ? Command.Action.CLOSE_RELAY : Command.Action.OPEN_RELAY;
            return commandDispatcher.dispatch(deviceId, new Command(action));
        });
        try {
            return result.get(2000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return Command.Result.PENDING;
        }
    }

    private Device getDevice(Session session, String deviceId) {
        Device device = session.bySimpleNaturalId(Device.class).load(deviceId);
        if (device == null) {
            throw new NotFoundException("device not found");
        }
        return device;
    }

    private Relay getRelay(Session session, String deviceId) {
        Device device = getDevice(session, deviceId);
        Relay relay = device.getRelay();
        if (relay == null) {
            throw new NotFoundException("relay not found");
        }
        return relay;
    }
}
