package com.sos.scheduler.plugins.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.sos.scheduler.plugins.globalmonitor.JobSchedulerFileElement;

public class TestJobSchedulerFileElement {

    @Test
    public void testGetSchedulerHome() {
        File f = new File("src/test/resources/config/live/myJob.job.xml");
        JobSchedulerFileElement jobSchedulerFileElement = new JobSchedulerFileElement(f);
        String s = jobSchedulerFileElement.getSchedulerLivePath();
        assertTrue("testGetSchedulerHome", s.endsWith("config/live"));
    }

    @Test
    public void testGetJobSchedulerElementName() {
        File f = new File("src/test/resources/config/live/myJob.job.xml");
        JobSchedulerFileElement jobSchedulerFileElement = new JobSchedulerFileElement(f);
        String s = jobSchedulerFileElement.getJobSchedulerElementName();
        assertEquals("testGetJobSchedulerElementName", "/myJob", s);
    }

}