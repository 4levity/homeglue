/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device.wemo;

import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.device.*;
import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.testing.HomeglueTests;
import net.forlevity.homeglue.util.ResourceHelper;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Log4j2
public class WemoInsightConnectorTest extends HomeglueTests {

    SimpleHttpClient httpClient;
    String hostAddress = "10.1.1.1";

    @Test
    public void testInitialConnect() throws IOException {
        int port = 45678;
        WemoInsightConnector connector = connectedConnector(port);
        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(httpClient).get(url.capture());
        assertEquals(String.format("http://%s:%d/setup.xml", hostAddress, port), url.getValue());
        assertEquals("94103E3D1A5C", connector.getDetectionId());
        assertEquals("coffeemaker", connector.getDetails().get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testRead() throws IOException {
        int port = 45678;
        WemoInsightConnector connector = connectedConnector(port);
        when(httpClient.post(any(),any(),any(),any()))
                .thenReturn(ResourceHelper.resourceAsString("net/forlevity/homeglue/sim/insightparams_response.xml"));
        ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String,String>> headers = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ContentType> contentType = ArgumentCaptor.forClass(ContentType.class);
        DeviceState state = connector.read();
        log.info("wemo driver read: {}", state);
        assertEquals(4.155, state.getInstantaneousWatts(), 0.001);
        verify(httpClient).post(url.capture(), headers.capture(), payload.capture(), contentType.capture());
        assertEquals(String.format("http://%s:%d/upnp/control/insight1",hostAddress,port), url.getValue());
        assertEquals("\"urn:Belkin:service:insight:1#GetInsightParams\"", headers.getValue().get("SOAPAction"));
        assertEquals(ResourceHelper.resourceAsString("net/forlevity/homeglue/sim/insightparams_request.xml"), payload.getValue());
        assertEquals(ContentType.TEXT_XML, contentType.getValue());
    }

    private WemoInsightConnector connectedConnector(int port) throws IOException {
        httpClient = mock(SimpleHttpClient.class);
        SoapHelper soapHelper = new SoapHelper(httpClient);
        WemoInsightConnector connector = new WemoInsightConnector(soapHelper, mock(DeviceConnectorInstances.class), mock(DeviceStateProcessorService.class), mock(OfflineMarkerService.class), hostAddress, port, mock(ScheduledExecutorService.class));
        when(httpClient.get(any())).thenReturn(ResourceHelper.resourceAsString("net/forlevity/homeglue/sim/insight1_setup.xml"));
        log.info("before connecting: {}", connector);
        assertTrue(connector.start());
        log.info("after connecting: {}", connector);
        return connector;
    }
}
