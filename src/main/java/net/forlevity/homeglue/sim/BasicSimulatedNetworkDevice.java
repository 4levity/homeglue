/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.sim;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Lo-fi simulation of LAN device with an IP address and web server.
 */
@Getter
public class BasicSimulatedNetworkDevice implements SimulatedNetworkDevice {

    public static final String ERROR_RESPONSE = "";

    private final InetAddress inetAddress;

    @Setter
    private int webPort;

    BasicSimulatedNetworkDevice(InetAddress inetAddress, int webPort) {
        this.inetAddress = inetAddress;
        this.webPort = webPort;
    }

    @Override
    public String get(String url) throws IOException {
        checkUrl(url);
        return ERROR_RESPONSE;
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType) throws IOException {
        checkUrl(url);
        return ERROR_RESPONSE;
    }

    private void checkUrl(String url) throws ConnectException, UnknownHostException {
        String prefix1 = String.format("http://%s:", getInetAddress().getHostAddress());
        String prefix2 = String.format("%s%d/", prefix1, getWebPort());
        if (!url.startsWith(prefix1)) {
            throw new UnknownHostException("unknown host for " + url);
        }
        if (!url.startsWith(prefix2)) {
            throw new ConnectException("connection refused (wrong port) for " + url);
        }
    }
}
