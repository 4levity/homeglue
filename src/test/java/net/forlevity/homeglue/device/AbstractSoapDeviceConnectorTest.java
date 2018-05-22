/*
 * Part of Homeglue (c) 2018 C. Ivan Cooper - https://github.com/4levity/homeglue
 * Homeglue is free software. You can modify and/or distribute it under the terms
 * of the Apache License Version 2.0: https://www.apache.org/licenses/LICENSE-2.0
 */

package net.forlevity.homeglue.device;

import net.forlevity.homeglue.HomeglueTests;
import net.forlevity.homeglue.http.SimpleHttpClient;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AbstractSoapDeviceConnectorTest extends HomeglueTests {

    @Test
    public void testXmlParsing() {
        TestSoapDeviceConnector connector = new TestSoapDeviceConnector();
        String xml = resourceAsString("insight1_setup.xml");
        Document document = connector.doParse(xml);
        assertNotNull(document);
        assertEquals(1, document.getElementsByTagName("manufacturer").getLength());
        assertEquals("urn:Belkin:device:insight:1", connector.getNodeText(document,"//deviceType"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSoapRequest() throws IOException {
        TestSoapDeviceConnector connector = new TestSoapDeviceConnector();
        SimpleHttpClient mockHttp = connector.httpClient();
        String url = "http://somewhere";
        String urn = "urn:something";
        String action = "DoNothing";
        when(mockHttp.post(any(),any(),any(),any()))
                .thenReturn(resourceAsString("insightparams_response.xml"));
        Document document = connector.doExecSoapRequest(url, urn, action);

        ArgumentCaptor<String> xmlRequest = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String,String>> requestHeaders = ArgumentCaptor.forClass(Map.class);
        verify(mockHttp).post(any(), requestHeaders.capture(), xmlRequest.capture(), any());
        Document requestDoc = connector.parse(xmlRequest.getValue());
        assertEquals(1, requestDoc.getElementsByTagName("u:DoNothing").getLength());
        assertEquals(String.format("\"%s#%s\"", urn, action), requestHeaders.getValue().get("SOAPAction"));
        assertEquals(11, connector.getNodeText(document, "//InsightParams").split("\\|").length);
    }

    private static class TestSoapDeviceConnector extends AbstractSoapDeviceConnector {

        protected TestSoapDeviceConnector() {
            super(mock(SimpleHttpClient.class));
        }

        @Override
        public boolean connect() {
            return false;
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        public SimpleHttpClient httpClient() {
            return getHttpClient();
        }

        public Document doParse(String xml) {
            return parse(xml);
        }

        public Document doExecSoapRequest(String url, String urn, String action) {
            return execSoapRequest(url, urn, action);
        }

        public String getNodeText(Document doc, String query) {
            return nodeText(doc, query);
        }
    }
}
