/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import net.forlevity.homeglue.HomeglueTests;
import net.forlevity.homeglue.device.PowerMeterData;
import net.forlevity.homeglue.http.SimpleHttpClient;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WemoInsightConnectorTest extends HomeglueTests {

    SimpleHttpClient httpClient;
    String hostAddress = "10.1.1.1";
    int port = 45678;

    @Test
    public void testInitialConnect() throws IOException {
        WemoInsightConnector connector = connectedConnector();
        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(httpClient).get(url.capture());
        assertEquals(String.format("http://%s:%d/setup.xml", hostAddress, port), url.getValue());
        assertEquals("94103E3D1A5C", connector.getDeviceId());
        assertEquals("coffeemaker", connector.getDeviceDetails().get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRead() throws IOException {
        WemoInsightConnector connector = connectedConnector();
        when(httpClient.post(any(),any(),any(),any()))
                .thenReturn(resourceAsString("insightparams_response.xml"));
        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String,String>> headers = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentType> contentType = ArgumentCaptor.forClass(ContentType.class);
        PowerMeterData data = connector.read();
        assertEquals(4.155, data.getInstantaneousWatts(), 0.001);
        // timestamp within 100ms of now:
        assertEquals(Instant.now().toEpochMilli() / 100.0, data.getTimestamp().toEpochMilli() / 100.0, 1.0);
        verify(httpClient).post(url.capture(), headers.capture(), payload.capture(), contentType.capture());
        assertEquals(String.format("http://%s:%d/upnp/control/insight1",hostAddress,port), url.getValue());
        assertEquals("\"urn:Belkin:service:insight:1#GetInsightParams\"", headers.getValue().get("SOAPAction"));
        assertEquals(resourceAsString("insightparams_request.xml"), payload.getValue());
        assertEquals(ContentType.TEXT_XML, contentType.getValue());
    }

    private WemoInsightConnector connectedConnector() throws IOException {
        httpClient = mock(SimpleHttpClient.class);
        WemoInsightConnector connector = new WemoInsightConnector(httpClient, hostAddress, port);
        when(httpClient.get(any())).thenReturn(resourceAsString("insight1_setup.xml"));
        assertTrue(connector.connect());
        return connector;
    }
}
