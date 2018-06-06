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
import net.forlevity.homeglue.entity.Relay;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Produces(MediaType.APPLICATION_JSON)
public class RelayResource {

    private final Relay relay;
    private final DeviceCommandDispatcher dispatcher;

    interface Factory {
        RelayResource create(Relay relay);
    }

    @Inject
    public RelayResource(DeviceCommandDispatcher dispatcher, @Assisted Relay relay) {
        this.relay = relay;
        this.dispatcher = dispatcher;
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
        Future<Command.Result> result = dispatcher.dispatch(relay.getDevice().getDetectionId(), new Command(action));
        try {
            return result.get(2000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            return Command.Result.PENDING;
        }
    }

}
