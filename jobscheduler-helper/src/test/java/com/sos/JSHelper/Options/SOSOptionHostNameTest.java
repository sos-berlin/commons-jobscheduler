package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

/** @author KB */
public class SOSOptionHostNameTest {

    private static final String CLASSNAME = "SOSOptionHostNameTest";
    private static final Logger LOGGER = Logger.getLogger(SOSOptionHostNameTest.class);
    public SOSOptionHostName objHostName = new SOSOptionHostName(null, CLASSNAME + ".variablename", "OptionDescription", "4444", "4444", true);

    public SOSOptionHostNameTest() {
        //
    }

    @Test
    public final void testValueString() {
        objHostName.Value(SOSOptionHostName.conLocalHostName);
        assertEquals("host is localhost", "localhost", objHostName.Value());
    }

    @Test
    public final void testGetHostAdress() {
        objHostName.Value(SOSOptionHostName.conLocalHostName);
        String strIPAdress = objHostName.getHostAdress();
        assertEquals("ip is 127.0.0.1", "127.0.0.1", strIPAdress);
    }

    @Test
    public final void testGetHostAdress2() {
        objHostName.Value("http://homer.sos/jade");
        String strIPAdress = objHostName.getHostAdress();
        assertEquals("ip is 192.11.0.95", "192.11.0.95", strIPAdress);
    }

    @Test
    public final void testToString() {
        objHostName.Value(SOSOptionHostName.conLocalHostName);
        String strIPAdress = objHostName.toString();
        assertEquals("expected: localhost (127.0.0.1)", "localhost (127.0.0.1)", strIPAdress);
    }

    @Test
    public final void testPing() {
        objHostName.Value(SOSOptionHostName.conLocalHostName);
        boolean flgHostIsReachable = objHostName.ping();
        assertTrue("Host is reachable", flgHostIsReachable);
    }

    @Test
    public final void testEmptyHostName() {
        objHostName.Value("");
        String strIPAdress = objHostName.toString();
        assertEquals("", "", strIPAdress);
        strIPAdress = objHostName.getHostAdress();
        assertEquals("", "", strIPAdress);
        strIPAdress = objHostName.getLocalHostIfHostIsEmpty();
        assertTrue("Host is " + strIPAdress, strIPAdress.length() > 0);
        strIPAdress = objHostName.getLocalHostAdressIfHostIsEmpty();
        assertTrue("IP is " + strIPAdress, strIPAdress.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"));
    }

    @Test
    public final void testgetLocalHostIfHostIsEmpty() {
        objHostName.Value("");
        String strIPAdress = objHostName.toString();
        assertEquals("", "", strIPAdress);
        strIPAdress = objHostName.getLocalHostIfHostIsEmpty();
        assertTrue("localhost", strIPAdress.length() > 0);
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public final void testPortOpen() {
        objHostName.Value(SOSOptionHostName.conLocalHostName);
        SOSOptionPortNumber objPort =
                new SOSOptionPortNumber(null, "port", "", String.valueOf(SOSOptionPortNumber.conPort4http),
                        String.valueOf(SOSOptionPortNumber.conPort4http), false);
        objHostName.setPort(objPort);
        assertTrue("Port 80 is available", objHostName.checkPortAvailable());
        objPort.value(4711);
        assertFalse("Port 4711 is not available", objHostName.checkPortAvailable());
    }

    @Test
    public void testGetPID() {
        LOGGER.info(objHostName.getPID());
    }

}