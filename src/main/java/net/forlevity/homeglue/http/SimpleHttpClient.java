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
