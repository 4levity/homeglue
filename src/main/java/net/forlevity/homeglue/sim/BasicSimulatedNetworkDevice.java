/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sim;

import lombok.Getter;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

/**
 * Lo-fi simulation of LAN device with an IP address and web server.
 */
@Getter
public class BasicSimulatedNetworkDevice implements SimulatedNetworkDevice {

    public static final String ERROR_RESPONSE = "error";

    protected final InetAddress inetAddress;

    BasicSimulatedNetworkDevice(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    @Override
    public String get(String url) throws IOException {
        return ERROR_RESPONSE;
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType) throws IOException {
        return ERROR_RESPONSE;
    }
}
