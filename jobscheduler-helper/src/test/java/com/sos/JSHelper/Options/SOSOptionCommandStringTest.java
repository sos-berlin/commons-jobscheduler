package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.io.Files.JSFile;

/** @author KB */
public class SOSOptionCommandStringTest {

    private static final Logger LOGGER = Logger.getLogger(SOSOptionCommandStringTest.class);
    private SOSOptionCommandString objCS = null;

    @Before
    public void setUp() throws Exception {
        objCS = new SOSOptionCommandString(null, "test", "Description", null, null, false);
    }

    @Test
    public final void testValue() {
        String strT = "Hello, world ...";
        objCS.Value(strT);
        assertEquals("strT", strT, objCS.Value());
    }

    @Test
    public final void testFromHexString() {
        String strV = "61626364";
        objCS.Value(strV);
        String strT = new String(objCS.fromHexString());
        assertEquals("must be equal", strT, "abcd");
    }

    @Test
    public final void testIsHex() {
        objCS.Value("ABCDEF");
        boolean flgT = objCS.isHex();
        assertTrue("must be true", flgT);
        objCS.Value("Hello");
        assertFalse("must be false", objCS.isHex());
        objCS.Value(null);
        assertFalse("must be false", objCS.isHex());
        objCS.Value("3132333435");
        LOGGER.info("Value is " + objCS.Value());
        assertEquals("Value is hex", "12345", objCS.Value());
    }

    @Test
    public void testValueString() throws Exception {
        JSOptionsClass objO = new JSOptionsClass();
        String strF = objO.TempDir() + "testSOSOptionFileString.txt";
        JSFile objF = new JSFile(strF);
        objF.deleteOnExit();
        String strT = "Select * from table;";
        objF.Write(strT);
        objF.close();
        objCS.Value(strF);
        LOGGER.info(objCS.Value());
        assertEquals("select", strT, objCS.Value());
    }

}