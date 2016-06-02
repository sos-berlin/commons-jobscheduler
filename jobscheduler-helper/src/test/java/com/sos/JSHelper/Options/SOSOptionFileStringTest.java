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
        String strF = objO.getTempDir() + "testSOSOptionFileString.txt";
        JSFile objF = new JSFile(strF);
        objF.deleteOnExit();
        String strT = "Select * from table;";
        objF.write(strT);
        objF.close();
        objCS.setValue(strF);
        LOGGER.info(objCS.getValue());
        assertEquals("select", strT, objCS.getValue());
    }

}