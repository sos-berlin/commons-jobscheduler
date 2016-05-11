package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.io.Files.JSFile;

public class SOSOptionFileStringTest {
    
    private static final Logger LOGGER = Logger.getLogger(SOSOptionFileStringTest.class);
    private SOSOptionFileString objCS = null;

    @Before
    public void setUp() throws Exception {
        objCS = new SOSOptionFileString(null, "test", "Description", null, null, false);
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