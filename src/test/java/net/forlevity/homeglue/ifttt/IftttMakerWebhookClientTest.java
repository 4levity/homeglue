/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.ifttt;

import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.testing.HomeglueTests;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class IftttMakerWebhookClientTest extends HomeglueTests {

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    public void testIftttClient() throws IOException {
        String iftttKey = "111222333444";
        String response = "Congratulations! You've fired a fake event to nowhere";
        SimpleHttpClient httpClient = mock(SimpleHttpClient.class);
        when(httpClient.post(any(), any(), any(), any())).thenReturn(response);
        IftttMakerWebhookClient client = new IftttMakerWebhookClient(httpClient, json, iftttKey);
        client.trigger(new IftttMakerWebhookClient.Event("a","a1", "a2", "a3"));
        client.trigger("b","b1", "b2", "b3");
        client.trigger("c", "c1", null, null);
        client.trigger("d");

        // verify correct looking webhooks are generated
        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> headers = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentType> contentType = ArgumentCaptor.forClass(ContentType.class);
        verify(httpClient, times(4)).post(url.capture(), headers.capture(), payload.capture(), contentType.capture());
        assertEquals(4, url.getAllValues().size());
        url.getAllValues().forEach(url1 -> {
            assertTrue(url1.startsWith("https://maker.ifttt.com/trigger/"));
            assertTrue(url1.contains(iftttKey));
        });
        headers.getAllValues().forEach(Assertions::assertNull);
        contentType.getAllValues().forEach(type -> assertEquals(ContentType.APPLICATION_JSON, type));

        // individual events correctly generated
        WebhookPostBody postBody = json.fromJson(payload.getAllValues().get(0), WebhookPostBody.class);
        assertTrue(url.getAllValues().get(0).contains("/trigger/a/"));
        assertEquals("a1", postBody.getValue1());
        assertEquals("a2", postBody.getValue2());
        assertEquals("a3", postBody.getValue3());

        postBody = json.fromJson(payload.getAllValues().get(1), WebhookPostBody.class);
        assertTrue(url.getAllValues().get(1).contains("/trigger/b/"));
        assertEquals("b1", postBody.getValue1());
        assertEquals("b2", postBody.getValue2());
        assertEquals("b3", postBody.getValue3());

        postBody = json.fromJson(payload.getAllValues().get(2), WebhookPostBody.class);
        assertTrue(url.getAllValues().get(2).contains("/trigger/c/"));
        assertEquals("c1", postBody.getValue1());
        assertNull(postBody.getValue2());
        assertNull(postBody.getValue3());

        assertTrue(url.getAllValues().get(3).contains("/trigger/d/"));
        assertEquals("", payload.getAllValues().get(3));
    }
}