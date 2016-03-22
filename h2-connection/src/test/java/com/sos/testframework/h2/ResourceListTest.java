package com.sos.testframework.h2;

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import static junit.framework.Assert.assertEquals;

public class ResourceListTest {

    private final static String testResource = "com/sos/testframework/h2/Table1.sql";

    @Test
    public void testFile() throws IOException {
        File f = File.createTempFile("myFile", ".txt");
        Files.append("this is a test.", f, Charset.defaultCharset());
        ResourceList r = new ResourceList();
        r.add("com.sos.mypackage.TestFile.txt", f);
        String dir = normalize(r.getWorkingDirectory());
        File result = r.getFilelist().get(0);
        assertEquals(normalize(f), normalize(result));
        assertEquals("this is a test.", Files.toString(result, Charset.defaultCharset()));
        r.release();
    }

    @Test
    public void testResource() throws IOException {
        ResourceList r = new ResourceList();
        r.add("com.sos.testframework.h2.Table1DBItem", "com/sos/testframework/h2/TestResource.txt");
        String dir = normalize(r.getWorkingDirectory());
        File result = r.getFilelist().get(0);
        assertEquals(dir + "/TestResource.txt", normalize(result));
        assertEquals("this is a test.", Files.toString(result, Charset.defaultCharset()));
        r.release();
    }

    @Test
    public void testURL() {
        URL url = Resources.getResource(testResource);
        ResourceList r = new ResourceList();
        r.add("com.sos.testframework.h2.Table1DBItem", url);
        String dir = normalize(r.getWorkingDirectory());
        File result = r.getFilelist().get(0);
        assertEquals(dir + "/Table1.sql", normalize(result));
        r.release();
    }

    private String normalize(File file) {
        return file.getAbsolutePath().replaceAll("\\\\", "/");
    }
}
