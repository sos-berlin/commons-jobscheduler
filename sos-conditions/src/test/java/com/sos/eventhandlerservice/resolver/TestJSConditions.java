package com.sos.eventhandlerservice.resolver;

import static org.junit.Assert.*;

import java.util.List;

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
        JSConditions jsConditions = new JSConditions();
        jsConditions.getListOfConditions("job:isStartedToday and job:/job5.isStartedToday  and job_chain:/job_chain1[start].isStartedToday");
    }

}
