package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** @author KB */
public class SOSOptionPortNumberTest {

    private static final String CLASSNAME = "SOSOptionPortNumberTest";
    public SOSOptionPortNumber objPortNumber = new SOSOptionPortNumber(null, CLASSNAME + ".variablename", "OptionDescription", "4444", "4444", true);

    public SOSOptionPortNumberTest() {
        //
    }

    @Test
    public final void testValueString() {
        objPortNumber.setValue("4711");
        assertEquals("port is 4711", 4711, objPortNumber.value());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public final void testValueString2() {
        objPortNumber.setValue("471111");
        assertEquals("port is 471111", 4711, objPortNumber.value());
    }

    @Test
    public final void testGetStandardSFTPPort() {
        assertEquals("sFTP port is 22", 22, SOSOptionPortNumber.getStandardSFTPPort());
    }

    @Test
    public final void testGetStandardFTPPort() {
        assertEquals("FTP port is 21", 21, SOSOptionPortNumber.getStandardFTPPort());
    }

}