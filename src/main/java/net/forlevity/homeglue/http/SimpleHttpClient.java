/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.http;

import com.google.inject.ImplementedBy;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.Map;

/**
 * A simple HTTP client interface for doing simple things with web services. Also useful for simulated network.
 */
@ImplementedBy(SimpleHttpClientImpl.class)
public interface SimpleHttpClient {

    /**
     * GET with default settings.
     *
     * @param url the url e.g. "https://github.com/"
     * @return response body as string (regardless of HTTP status code)
     * @throws IOException on network problems
     */
    String get(String url) throws IOException;

    /**
     * POST to a web service.
     *
     * @param url the url
     * @param headers additional headers, or null for none
     * @param payload payload to send
     * @param contentType content type of payload
     * @return response body as string (regardless of HTTP status code)
     * @throws IOException on network problems
     */
    String post(String url, Map<String, String> headers, String payload, ContentType contentType) throws IOException;
}
