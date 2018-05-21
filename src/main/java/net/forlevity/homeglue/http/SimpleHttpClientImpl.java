package net.forlevity.homeglue.http;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;

import java.io.IOException;
import java.util.Map;

public class SimpleHttpClientImpl implements SimpleHttpClient {

    @Override
    public String get(String location) throws IOException {
        return Request.Get(location).execute().returnContent().asString();
    }

    @Override
    public String post(String url, Map<String, String> headers, String payload, ContentType contentType)
            throws IOException {
        Request request = Request.Post(url);
        headers.forEach((name, value) -> request.setHeader(name, value));
        request.bodyString(payload, ContentType.TEXT_XML);
        return request.execute().returnContent().asString();
    }
}
