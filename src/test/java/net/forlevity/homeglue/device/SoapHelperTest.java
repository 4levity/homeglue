/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import net.forlevity.homeglue.http.SimpleHttpClient;
import net.forlevity.homeglue.testing.HomeglueTests;
import net.forlevity.homeglue.util.ResourceHelper;
import net.forlevity.homeglue.util.Xml;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SoapHelperTest extends HomeglueTests {

    final Xml xml = new Xml();

    @Test
    public void testXmlParsing() {
        SoapHelper soapHelper = new SoapHelper(mock(SimpleHttpClient.class));
        String xmlText = ResourceHelper.resourceAsString("net/forlevity/homeglue/sim/insight1_setup.xml");
        Document document = xml.parse(xmlText);
        assertNotNull(document);
        assertEquals(1, document.getElementsByTagName("manufacturer").getLength());
        assertEquals("urn:Belkin:device:insight:1", xml.nodeText(document,"//deviceType"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSoapRequest() throws IOException {
        SimpleHttpClient mockHttp = mock(SimpleHttpClient.class);
        String url = "http://somewhere";
        String urn = "urn:something";
        String action = "DoNothing";
        when(mockHttp.post(any(),any(),any(),any()))
                .thenReturn(ResourceHelper.resourceAsString("net/forlevity/homeglue/sim/insightparams_response.xml"));
        SoapHelper soapHelper = new SoapHelper(mockHttp);
        Document document = soapHelper.execSoapRequest(url, urn, action, "");

        ArgumentCaptor<String> xmlRequest = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String,String>> requestHeaders = ArgumentCaptor.forClass(Map.class);
        verify(mockHttp).post(any(), requestHeaders.capture(), xmlRequest.capture(), any());
        Document requestDoc = xml.parse(xmlRequest.getValue());
        assertEquals(1, requestDoc.getElementsByTagName("u:DoNothing").getLength());
        assertEquals(String.format("\"%s#%s\"", urn, action), requestHeaders.getValue().get("SOAPAction"));
        assertEquals(11, xml.nodeText(document, "//InsightParams").split("\\|").length);
    }
}
