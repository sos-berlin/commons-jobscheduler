package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class SOSOptionUrlTest {

    private SOSOptionUrl objU = new SOSOptionUrl(null, "url", "descr", "", "", false);

    @Test
    public void testValueString() {
        objU.setValue("ftp://kb:kb@homer.sos/home/test/test.txt");
        assertEquals("folder name", "/home/test/test.txt", objU.getFolderName());
        objU.setValue("ftp://kb:kb@homer.sos/./test/test.txt");
        assertEquals("folder name", "/./test/test.txt", objU.getFolderName());
    }

    @Test
    public void testValueString2() {
        objU.setValue("file:///./JCLs");
        assertEquals("folder name", "/./JCLs", objU.getFolderName());
    }

    @Test
    public void testValueString3() {
        objU.setValue("file:///src/test/resources/JCLs");
        new File("src/test/resources/JCLs").mkdir();
        assertEquals("folder name", "/src/test/resources/JCLs", objU.getFolderName());
        assertTrue("dir exists", new File("src/test/resources/JCLs").exists());
    }

    @Test
    public void testSOSOptionUrl() {
        objU = new SOSOptionUrl(null, "url", "descr", "", "", false);
    }

}