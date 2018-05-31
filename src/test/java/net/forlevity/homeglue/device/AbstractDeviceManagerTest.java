package net.forlevity.homeglue.device;

import lombok.extern.log4j.Log4j2;
import net.forlevity.homeglue.testing.HomeglueTests;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Log4j2
public class AbstractDeviceManagerTest extends HomeglueTests {

    @Test
    public void testRegisterDevices() {
        TestDeviceManager dm = new TestDeviceManager();
        assertTrue(dm.getDevices().isEmpty());
        DeviceConnector device1 = mock(DeviceConnector.class);
        when(device1.getDeviceId()).thenReturn("1");
        dm.testRegister(device1);
        assertEquals(1, dm.getDevices().size());
        assertEquals("1", dm.getDevices().iterator().next().getDeviceId());
        DeviceConnector device2 = mock(DeviceConnector.class);
        when(device2.getDeviceId()).thenReturn("2");
        dm.testRegister(device2);
        assertEquals(2, dm.getDevices().size());

        DeviceConnector device2duplicate = mock(DeviceConnector.class);
        when(device2duplicate.getDeviceId()).thenReturn("2");
        dm.testRegister(device2);
        assertEquals(2, dm.getDevices().size()); // did not add third device
    }

    private static class TestDeviceManager extends AbstractDeviceManager {

        protected TestDeviceManager() {
            super(status -> log.info("{}", status));
        }

        public void testRegister(DeviceConnector dc) {
            this.reportStatus(dc);
        }
    }
}
