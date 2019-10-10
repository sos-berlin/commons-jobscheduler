package com.sos.jobstreams.resolver;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TestJSConditions {

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
    public void test() {
        JSConditions.getListOfConditions("job:isStartedToday event:test2(yesterday) and job:/job5.isStartedToday  and job_chain:/job_chain1[start].isStartedToday");
    }
    
    @Test
    public void testNormalize() {
        String s = JSConditions.normalizeExpression("job:isStartedToday and job:/job5.isStartedToday and event:test1 and test2 and global:test3[yesterday] and job_chain:/job_chain1[start].isStartedToday");
        assertEquals("testNormalize", "job:isStartedToday and job:/job5.isStartedToday and event:test1 and event:test2 and global:test3[yesterday] and job_chain:/job_chain1[start].isStartedToday", s);

         
    }

}
