/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.http;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.Map;

/**
 * Standard implementation for SimpleHttpClient.
 */
@Singleton
public class SimpleHttpClientImpl implements SimpleHttpClient {

    private final int connectTimeoutMillis;
    private final int socketTimeoutMillis;

    @Inject
    public SimpleHttpClientImpl(@Named("http.connect.timeout.millis") int connectTimeoutMillis,
                                @Named("http.socket.timeout.millis") int socketTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.socketTimeoutMillis = socketTimeoutMillis;
    }

    @Override
    public String get(String url) throws IOException {
        return Request.Get(url)
                .connectTimeout(connectTimeoutMillis)
                .socketTimeout(socketTimeoutMillis)
                .execute().returnContent().asString();
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType)
            throws IOException {
        Request request = Request.Post(url)
                .connectTimeout(connectTimeoutMillis)
                .socketTimeout(socketTimeoutMillis);
        if (headers != null) {
            headers.forEach((name, value) -> request.setHeader(name, value));
        }
        request.bodyString(payload, contentType);
        return request.execute().returnContent().asString();
    }
}
