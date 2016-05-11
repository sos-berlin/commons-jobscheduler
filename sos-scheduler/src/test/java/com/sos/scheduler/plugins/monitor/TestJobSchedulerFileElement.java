package com.sos.scheduler.plugins.monitor;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.sos.scheduler.plugins.globalmonitor.JobSchedulerFileElement;

public class TestJobSchedulerFileElement {

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