package com.sos.jobstreams.resolver;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestReturnCodeResolver {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testResolveReturnCode() {
        JSReturnCodeResolver jsReturnCodeResolver = new JSReturnCodeResolver();
        boolean b = jsReturnCodeResolver.resolve(1, "1"); 
        assertEquals("testResolveReturnCode1", true, b);
        b = jsReturnCodeResolver.resolve(1, "0"); 
        assertEquals("testResolveReturnCode2", false, b);
        b = jsReturnCodeResolver.resolve(1, "1-5"); 
        assertEquals("testResolveReturnCod3e", true, b);
        b = jsReturnCodeResolver.resolve(1, "0-3"); 
        assertEquals("testResolveReturnCode4", true, b);
        b = jsReturnCodeResolver.resolve(1, "2-5"); 
        assertEquals("testResolveReturnCode6", false, b);
        b = jsReturnCodeResolver.resolve(1, "-3"); 
        assertEquals("testResolveReturnCode7", true, b);
        b = jsReturnCodeResolver.resolve(1, "3-"); 
        assertEquals("testResolveReturnCode8", false, b);
        b = jsReturnCodeResolver.resolve(1, "1-"); 
        assertEquals("testResolveReturnCode9", true, b);
        b = jsReturnCodeResolver.resolve(1, "-1"); 
        assertEquals("testResolveReturnCode10", true, b);
        b = jsReturnCodeResolver.resolve(1, "2,3,4"); 
        assertEquals("testResolveReturnCode11", false, b);
        b = jsReturnCodeResolver.resolve(1, "6,7,1-5"); 
        assertEquals("testResolveReturnCode12", true, b);
        b = jsReturnCodeResolver.resolve(1, "1,6,7"); 
        assertEquals("testResolveReturnCode13", true, b);
        b = jsReturnCodeResolver.resolve(1, "-5,-4,-1"); 
        assertEquals("testResolveReturnCode14", true, b);
        b = jsReturnCodeResolver.resolve(1, "2-5,-4"); 
        assertEquals("testResolveReturnCode15", true, b);
        b = jsReturnCodeResolver.resolve(1, "2-3,-4"); 
        assertEquals("testResolveReturnCode16", true, b);
    }

}
