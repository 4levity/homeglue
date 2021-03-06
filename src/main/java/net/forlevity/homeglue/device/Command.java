/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Command {

    private final Action action;

    public enum Action {
        CLOSE_RELAY,
        OPEN_RELAY
    }

    public enum Result {
        NONE,
        PENDING,
        NOT_SUPPORTED,
        SUCCESS,
        DEVICE_NOT_FOUND,
        COMMS_FAILED,
        DEVICE_ERROR,
        CONNECTOR_ERROR
    }
}
