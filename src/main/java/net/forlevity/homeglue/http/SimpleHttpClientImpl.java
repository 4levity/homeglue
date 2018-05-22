/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.http;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.Map;

public class SimpleHttpClientImpl implements SimpleHttpClient {

    private static final int CONNECT_TIMEOUT_MILLLIS = 5000;
    private static final int SOCKET_TIMEOUT_MILLIS = 5000;

    @Override
    public String get(String location) throws IOException {
        return Request.Get(location)
                .connectTimeout(CONNECT_TIMEOUT_MILLLIS)
                .socketTimeout(SOCKET_TIMEOUT_MILLIS)
                .execute().returnContent().asString();
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType)
            throws IOException {
        Request request = Request.Post(url)
                .connectTimeout(CONNECT_TIMEOUT_MILLLIS)
                .socketTimeout(SOCKET_TIMEOUT_MILLIS);
        headers.forEach((name, value) -> request.setHeader(name, value));
        request.bodyString(payload, ContentType.TEXT_XML);
        return request.execute().returnContent().asString();
    }
}
