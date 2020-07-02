package com.sos.JSHelper.io.Files;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Listener.JSListenerClass;

public class JSCsvFileTest extends JSListenerClass {

    private static JSFile fleTestdataDirectory = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        fleTestdataDirectory = new JSFile("R:/backup/sos/java/junittests/testdata/JSCsvFileTest/");
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testReadCsvFile1() throws Exception {
        String[] headers = null;
        String[] strValues = null;
        String strF = fleTestdataDirectory + "/StoppageList.csv";
        JSCsvFile objF = new JSCsvFile(strF);
        objF.setCheckColumnCount(false);
        objF.deleteOnExit();
        objF.writeLine("Reporter;StoppageType;FromDate;ToDate");
        objF.writeLine("## StoppageType: S=Stop, R=Run; ; ; ");
        objF.writeLine("### Inbound; ");
        objF.writeLine("# P2R             ; ; ; ");
        objF.writeLine("2001            ;S;        13.02.2009 23:00:00;15.02.2009 22:00:00");
        objF.close();
        JSCsvFile objCsvFile = new JSCsvFile(fleTestdataDirectory + "/StoppageList.csv");
        objCsvFile.setCheckColumnCount(false);
        objCsvFile.loadHeaders();
        headers = objCsvFile.getHeaders();
        assertEquals("Header1 must be Reporter", headers[0], "Reporter");
        assertEquals("Header2 must be StoppageType", headers[1], "StoppageType");
        assertEquals("Header3 must be FromDate", headers[2], "FromDate");
        assertEquals("Header4 must be ToDate", headers[3], "ToDate");
        strValues = objCsvFile.readCSVLine();
        assertEquals("Field1 must be ## StoppageType: S=Stop, R=Run", strValues[0], "## StoppageType: S=Stop, R=Run");
        strValues = objCsvFile.readCSVLine();
        assertEquals("Field1 must be ### Inbound: S=Stop, R=Run", strValues[0], "### Inbound");
        strValues = objCsvFile.readCSVLine();
        assertEquals("Field1 must be # P2R             ", strValues[0], "# P2R             ");
        strValues = objCsvFile.readCSVLine();
        assertEquals("Field1 must be 2001            ", strValues[0], "2001            ");
        assertEquals("Field2 must be S", strValues[1], "S");
        assertEquals("Field3 must be         13.02.2009 23:00:00", strValues[2], "        13.02.2009 23:00:00");
        assertEquals("Field4 must be 15.02.2009 22:00:00", strValues[3], "15.02.2009 22:00:00");
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testReadCsvFile2() throws Exception {
        String[] headers = null;
        String[] strValues = null;
        JSCsvFile objCsvFile = new JSCsvFile(fleTestdataDirectory + "/test.csv");
        objCsvFile.registerMessageListener(this);
        objCsvFile.loadHeaders();
        headers = objCsvFile.getHeaders();
        assertEquals("Header1 must be Reporter", headers[0], "Reporter");
        assertEquals("Header2 must be StoppageType", headers[1], "StoppageType");
        assertEquals("Header3 must be FromDate", headers[2], "FromDate");
        assertEquals("Header4 must be ToDate", headers[3], "ToDate");
        strValues = objCsvFile.readCSVLine();
        assertEquals("Field1 must be 2001            ", strValues[0], "2001            ");
        assertEquals("Field2 must be S", strValues[1], "S");
        assertEquals("Field3 must be         13.02.2009 23:00:00", strValues[2], "        13.02.2009 23:00:00");
        assertEquals("Field4 must be 15.02.2009 22:00:00", strValues[3], "15.02.2009 22:00:00");
        strValues = objCsvFile.readCSVLine();
        assertEquals("Field1 must be 0032            ", strValues[0], "0032            ");
        assertEquals("Field2 must be S", strValues[1], "S");
        assertEquals("Field3 must be         13.02.2009 23:00:00", strValues[2], "        13.02.2009 23:00:00");
        assertEquals("Field4 must be 15.02.2009 22:00:00", strValues[3], "15.02.2009 22:00:00");
    }

}