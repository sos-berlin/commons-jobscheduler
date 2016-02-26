package com.sos.JSHelper.Options;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.io.Files.JSFile;

/** @author KB */
public class SOSOptionPasswordTest {

    private static final Logger LOGGER = Logger.getLogger(SOSOptionPasswordTest.class);
    private SOSOptionPassword objOption = null;

    public SOSOptionPasswordTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        String strLog4JFileName = "./log4j.properties";
        LOGGER.info("logfilename = " + new File(strLog4JFileName).getAbsolutePath());
        objOption = new SOSOptionPassword(null, "key", "Description", "value", "DefaultValue", true);
    }

    @Test
    public final void testToString() {
        objOption.Value("huhu");
        org.junit.Assert.assertEquals("password", "huhu", objOption.Value());
        String strFileName = CreateTestFile();
        objOption.Value(SOSOptionPassword.conBackTic + strFileName + SOSOptionPassword.conBackTic);
        org.junit.Assert.assertEquals("password", "huhu", objOption.Value());
    }

    private String CreateTestFile() {
        String strFileName = System.getProperty("java.io.tmpdir") + "test.cmd";
        JSFile objFile = new JSFile(strFileName);
        objFile.deleteOnExit();
        try {
            objFile.WriteLine("@echo off");
            objFile.WriteLine("echo huhu");
            objFile.close();
            objFile.setExecutable(true);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return strFileName;
    }

    @Test
    public final void testValueString() {
        objOption.Value("[uuid:]");
        LOGGER.debug(objOption.Value());
        objOption.Value("[env:username]");
        LOGGER.debug(objOption.Value());
        String strFilenName = CreateTestFile();
        objOption.Value("[shell:" + strFilenName + "]");
        LOGGER.debug(objOption.Value());
    }

}
