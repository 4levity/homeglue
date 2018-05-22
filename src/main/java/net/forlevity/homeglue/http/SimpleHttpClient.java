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

@ImplementedBy(SimpleHttpClientImpl.class)
public interface SimpleHttpClient {

    String get(String location) throws IOException;

    String post(String url, Map<String, String> headers, String payload, ContentType contentType) throws IOException;
}
