package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class SOSOptionUrlTest {

    private SOSOptionUrl objU = new SOSOptionUrl(null, "url", "descr", "", "", false);

    @Test
    public void testValueString() {
        objU.Value("ftp://kb:kb@homer.sos/home/test/test.txt");
        assertEquals("folder name", "/home/test/test.txt", objU.getFolderName());
        objU.Value("ftp://kb:kb@homer.sos/./test/test.txt");
        assertEquals("folder name", "/./test/test.txt", objU.getFolderName());
    }

    @Test
    public void testValueString2() {
        objU.Value("file:///./JCLs");
        assertEquals("folder name", "/./JCLs", objU.getFolderName());
    }

    @Test
    public void testValueString3() {
        objU.Value("file:///c:/temp/JCLs");
        new File("c:/temp/JCLs").mkdir();
        assertEquals("folder name", "/c:/temp/JCLs", objU.getFolderName());
        assertTrue("dir exists", new File(objU.getFolderName()).exists());
    }

    @Test
    public void testSOSOptionUrl() {
        objU = new SOSOptionUrl(null, "url", "descr", "", "", false);
    }

}