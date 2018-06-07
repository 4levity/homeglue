/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.api;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import net.forlevity.homeglue.device.Command;
import net.forlevity.homeglue.device.DeviceConnectorInstances;
import net.forlevity.homeglue.entity.Relay;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Produces(MediaType.APPLICATION_JSON)
public class RelayResource {

    private static final long API_DEVICE_COMMAND_TIMEOUT_MILLIS = 4000;

    private final Relay relay;
    private final DeviceConnectorInstances registry;

    interface Factory {
        RelayResource create(Relay relay);
    }

    @Inject
    public RelayResource(DeviceConnectorInstances registry, @Assisted Relay relay) {
        this.relay = relay;
        this.registry = registry;
    }

    @GET
    public RelayDto getRelayState() {
        return RelayDto.from(relay);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Command.Result setRelayState(RelayDto newState) throws ExecutionException, InterruptedException {
        if (newState == null) {
            throw new BadRequestException("relay state must be true (closed) or false (open)");
        }
        Command.Action action = newState.isClosed() ? Command.Action.CLOSE_RELAY : Command.Action.OPEN_RELAY;
        Future<Command.Result> result = registry.dispatch(relay.getDevice().getDetectionId(), new Command(action));
        try {
            return result.get(API_DEVICE_COMMAND_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return Command.Result.PENDING;
        }
    }

}
