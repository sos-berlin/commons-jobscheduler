package com.sos.VirtualFileSystem.common;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class SOSFileEntryTest {

    private static final String INIT_PARENT_PATH = "/home/test";
    private static final int INIT_FILESIZE = 8888;
    private static final String INIT_FILENAME = "my_filename.txt";
    private SOSFileEntry sosFileEntry;

    @Before
    public void setUp() throws Exception {
        sosFileEntry = new SOSFileEntry();
        sosFileEntry.setDirectory(false);
        sosFileEntry.setFilename(INIT_FILENAME);
        sosFileEntry.setFilesize(4711);
        sosFileEntry.setParentPath(INIT_PARENT_PATH);
    }

    @Test
    public void testGetFilename() {
        sosFileEntry.setFilename(INIT_FILENAME);
        assertEquals("testGetFilename unexpected value", INIT_FILENAME, sosFileEntry.getFilename());
    }

    @Test
    public void testSetFilename() {
        sosFileEntry.setFilename(INIT_FILENAME + ".new");
        assertEquals("testGetFilename unexpected value", INIT_FILENAME + ".new", sosFileEntry.getFilename());
    }

    @Test
    public void testGetFilesize() {
        sosFileEntry.setFilesize(INIT_FILESIZE);
        assertEquals("testGetFilesize unexpected value", INIT_FILESIZE, sosFileEntry.getFilesize());
    }

    @Test
    public void testSetFilesize() {
        sosFileEntry.setFilesize(INIT_FILESIZE + 3);
        assertEquals("testSetFilesize unexpected value", INIT_FILESIZE + 3, sosFileEntry.getFilesize());
    }

    @Test
    public void testIsDirectory() {
        sosFileEntry.setDirectory(false);
        assertEquals("testIsDirectory unexpected value", false, sosFileEntry.isDirectory());
    }

    @Test
    public void testSetDirectory() {
        sosFileEntry.setDirectory(true);
        assertEquals("testSetDirectory unexpected value", true, sosFileEntry.isDirectory());
    }

    @Test
    public void testGetParentPath() {
        sosFileEntry.setParentPath(INIT_PARENT_PATH + "**");
        assertEquals("testGetParentPath unexpected value", INIT_PARENT_PATH + "**", sosFileEntry.getParentPath());
    }

    @Test
    public void testSetParentPath() {
        sosFileEntry.setParentPath(INIT_PARENT_PATH + "**");
        assertEquals("testSetParentPath unexpected value", INIT_PARENT_PATH + "**", sosFileEntry.getParentPath());
    }

    @Test
    public void testGetFullPath() {
        sosFileEntry.setParentPath(INIT_PARENT_PATH);
        sosFileEntry.setFilename(INIT_FILENAME);
        assertEquals("testGetFullPath unexpected value", INIT_PARENT_PATH + "/" + INIT_FILENAME, sosFileEntry.getFullPath());
    }

    @Test
    public void testIsDirUp() {
        sosFileEntry.setFilename("..");
        assertEquals("testIsDirUp unexpected value", true, sosFileEntry.isDirUp());
    }

    @Test
    public void testGetFilesizeAsString() {
        sosFileEntry.setFilesize(4711);
        assertEquals("testGetFilesizeAsString unexpected value", "4711", sosFileEntry.getFilesizeAsString());
    }

    @Test
    public void testGetCategory() {
        sosFileEntry.setDirectory(true);
        assertEquals("testGetCategory unexpected value", "Folder", sosFileEntry.getCategory());
        sosFileEntry.setDirectory(false);
        assertEquals("testGetCategory unexpected value", "File", sosFileEntry.getCategory());
    }

    @Test
    public void testIsFileOrFolder() {
        sosFileEntry.setFilename(null);
        assertEquals("testIsFileOrFolder unexpected value", false, sosFileEntry.isFileOrFolder());
        sosFileEntry.setFilename(".");
        assertEquals("testIsFileOrFolder unexpected value", false, sosFileEntry.isFileOrFolder());
        sosFileEntry.setFilename("..");
        assertEquals("testIsFileOrFolder unexpected value", false, sosFileEntry.isFileOrFolder());
        sosFileEntry.setFilename("");
        assertEquals("testIsFileOrFolder unexpected value", false, sosFileEntry.isFileOrFolder());
        sosFileEntry.setFilename(INIT_FILENAME);
        assertEquals("testIsFileOrFolder unexpected value", true, sosFileEntry.isFileOrFolder());
    }

}