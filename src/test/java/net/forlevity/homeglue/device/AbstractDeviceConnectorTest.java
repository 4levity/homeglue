package net.forlevity.homeglue.device;

import net.forlevity.homeglue.HomeglueTests;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractDeviceConnectorTest extends HomeglueTests {

    @Test
    public void setDeviceDetails() {
        TestDeviceConnector dc = new TestDeviceConnector();
        assertTrue(dc.getDeviceDetails().isEmpty());
        dc.testSetDetail("test1","value1");
        dc.testSetDetail("test2","value2");
        assertEquals(2, dc.getDeviceDetails().size());
        dc.testSetDetail("test2","value2x");
        assertEquals(2, dc.getDeviceDetails().size());
        assertEquals("value1", dc.getDeviceDetails().get("test1"));
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

        public void testSetDetail(String name, String value) {
            this.setDeviceDetail(name, value);
        }

        public void testSetId(String id) {
            this.setDeviceId(id);
        }
    }
}
