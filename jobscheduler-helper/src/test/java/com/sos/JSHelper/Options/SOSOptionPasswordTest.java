package com.sos.JSHelper.Options;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.io.Files.JSFile;

public class SOSOptionPasswordTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSOptionPasswordTest.class);
    private SOSOptionPassword objOption = null;

    @Before
    public void setUp() throws Exception {
        String strLog4JFileName = "./log4j.properties";
        LOGGER.info("logfilename = " + new File(strLog4JFileName).getAbsolutePath());
        objOption = new SOSOptionPassword(null, "key", "Description", "value", "DefaultValue", true);
    }

    @Test
    public final void testToString() {
        objOption.setValue("huhu");
        org.junit.Assert.assertEquals("password", "huhu", objOption.getValue());
        String strFileName = createTestFile();
        objOption.setValue(SOSOptionPassword.conBackTic + strFileName + SOSOptionPassword.conBackTic);
        org.junit.Assert.assertEquals("password", "huhu", objOption.getValue());
    }

    private String createTestFile() {
        File f = new File("C:/Windows/system.ini");
        String strFileName;
        if (f.exists()){
            strFileName = System.getProperty("java.io.tmpdir") + "/test.cmd";
        } else {
            strFileName = System.getProperty("java.io.tmpdir") + "/test.sh";
        }
        JSFile objFile = new JSFile(strFileName);
        objFile.deleteOnExit();
        try {
            objFile.writeLine("@echo off");
            objFile.writeLine("echo huhu");
            objFile.close();
            objFile.setExecutable(true);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return strFileName;
    }

    @Test
    public final void testValueString() {
        objOption.setValue("[uuid:]");
        LOGGER.debug(objOption.getValue());
        objOption.setValue("[env:username]");
        LOGGER.debug(objOption.getValue());
        String strFilenName = createTestFile();
        objOption.setValue("[shell:" + strFilenName + "]");
        LOGGER.debug(objOption.getValue());
    }

}