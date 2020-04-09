package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.io.Files.JSFile;

/** @author KB */
public class SOSOptionCommandStringTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOptionCommandStringTest.class);
    private SOSOptionCommandString objCS = null;

    @Before
    public void setUp() throws Exception {
        objCS = new SOSOptionCommandString(null, "test", "Description", null, null, false);
    }

    @Test
    public final void testValue() {
        String strT = "Hello, world ...";
        objCS.setValue(strT);
        assertEquals("strT", strT, objCS.getValue());
    }

    @Test
    public final void testFromHexString() {
        String strV = "61626364";
        objCS.setValue(strV);
        String strT = new String(objCS.fromHexString());
        assertEquals("must be equal", strT, "abcd");
    }

    @Test
    public final void testIsHex() {
        objCS.setValue("ABCDEF");
        boolean flgT = objCS.isHex();
        assertTrue("must be true", flgT);
        objCS.setValue("Hello");
        assertFalse("must be false", objCS.isHex());
        objCS.setValue(null);
        assertFalse("must be false", objCS.isHex());
        objCS.setValue("3132333435");
        LOGGER.info("Value is " + objCS.getValue());
        assertEquals("Value is hex", "12345", objCS.getValue());
    }

    
}