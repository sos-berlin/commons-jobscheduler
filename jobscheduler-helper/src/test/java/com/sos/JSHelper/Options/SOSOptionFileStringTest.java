package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.JSHelper.io.Files.JSFile;

public class SOSOptionFileStringTest {

    @SuppressWarnings("unused")
    private final String conClassName = "SOSOptionFileStringTest";
    private static final String conSVNVersion = "$Id$";
    private SOSOptionFileString objCS = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        objCS = new SOSOptionFileString(null, "test", "Description", null, null, false);

    }

    @After
    public void tearDown() throws Exception {
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

        objCS.Value(strF); // the filename is the value
        System.out.println(objCS.Value());
        assertEquals("select", strT, objCS.Value());
    }

    @Test
    public void testSOSOptionFileString() {
    }

}
