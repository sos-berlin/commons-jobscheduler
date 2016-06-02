package com.sos.JSHelper.io.Files;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/** @author KB */
public class JSFolderTest {

    @Test
    public void testGetFilelist() {
        JSFolder objF = JSFolder.getTempDir();
        objF.getFilelist("\\.txt", 0);
    }

    @Test
    public void testNewFile() throws IOException {
        JSFolder objF = JSFolder.getTempDir();
        JSFile objFle = objF.getNewFile("testNewFile.tmp");
        objFle.writeLine("testNewFile.tmp");
        assertTrue("File must exist", objFle.exists());
        objFle.delete();
    }

}