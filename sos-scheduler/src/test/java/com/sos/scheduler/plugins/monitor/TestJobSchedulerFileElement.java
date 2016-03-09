package com.sos.scheduler.plugins.monitor;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.scheduler.plugins.globalmonitor.JobSchedulerFileElement;

public class TestJobSchedulerFileElement {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetSchedulerHome() {
        File f = new File("c:\\config\\live\\xxx\\job.job.xml");
        JobSchedulerFileElement jobSchedulerFileElement = new JobSchedulerFileElement(f);
        String s = jobSchedulerFileElement.getSchedulerLivePath();
        assertEquals("testGetSchedulerHome", "c:/config/live", s);
    }

    @Test
    public void testGetJobSchedulerElementName() {
        File f = new File("c:\\config\\live\\xxx\\job.job.xml");
        JobSchedulerFileElement jobSchedulerFileElement = new JobSchedulerFileElement(f);
        String s = jobSchedulerFileElement.getJobSchedulerElementName();
        assertEquals("testGetJobSchedulerElementName", "/xxx/job", s);
    }

}
