package com.sos.JSHelper.Options;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class SOSOptionFolderNameTest {

    private SOSOptionFolderName objCS = null;

    @Before
    public void setUp() throws Exception {
        objCS = new SOSOptionFolderName(null, "test", "Description", null, null, false);
    }

    @Test
    public void testRelativeFolderNames() {
        objCS.setValue(".");
        assertEquals("relative .", ".", objCS.getValue());
    }

    @Test
    public void testisDotFolder() {
        objCS.setValue(".");
        assertTrue("relative .", objCS.isDotFolder());
        objCS.setValue("..");
        assertTrue("relative ..", objCS.isDotFolder());
        objCS.setValue("/tmp/test");
        assertFalse("absolute .", objCS.isDotFolder());
    }

    @Test
    public void getSubFolderArrayTest() {
        objCS.setValue("/a/b/c/d");
        String[] strT = objCS.getSubFolderArray();
        assertTrue("wrong number of folders", strT.length == 4);
        assertEquals("ungleich", "/a", strT[0]);
        assertEquals("ungleich", "/a/b", strT[1]);
        assertEquals("ungleich", "/a/b/c", strT[2]);
        assertEquals("ungleich", "/a/b/c/d", strT[3]);
    }

}