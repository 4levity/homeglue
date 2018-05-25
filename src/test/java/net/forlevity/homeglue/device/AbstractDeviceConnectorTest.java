package net.forlevity.homeglue.device;

import com.google.common.collect.ImmutableMap;
import net.forlevity.homeglue.testing.HomeglueTests;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractDeviceConnectorTest extends HomeglueTests {

    @Test
    public void setDeviceDetails() {
        TestDeviceConnector dc = new TestDeviceConnector();
        assertTrue(dc.getDeviceDetails().isEmpty());
        assertEquals(DeviceConnector.DEVICE_ID_UNKNOWN, dc.getDeviceId());
        dc.testSetDetails(ImmutableMap.of("test1","value1","test2","value2"));
        assertEquals(2, dc.getDeviceDetails().size());
        dc.testSetDetails(ImmutableMap.of("test2","value2x"));
        assertEquals(1, dc.getDeviceDetails().size());
        assertEquals(null, dc.getDeviceDetails().get("test1"));
        assertEquals("value2x", dc.getDeviceDetails().get("test2"));
    }

    @Test
    public void setDeviceId() {
        TestDeviceConnector dc = new TestDeviceConnector();
        assertEquals(DeviceConnector.DEVICE_ID_UNKNOWN, dc.getDeviceId());
        dc.testSetId("newId");
        assertEquals("newId", dc.getDeviceId());
    }

    private static class TestDeviceConnector extends AbstractDeviceConnector {
        @Override
        public boolean connect() {
            return false;
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        public void testSetDetails(Map<String, String> map) {
            this.setDeviceDetails(map);
        }

        public void testSetId(String id) {
            this.setDeviceId(id);
        }
    }
}
