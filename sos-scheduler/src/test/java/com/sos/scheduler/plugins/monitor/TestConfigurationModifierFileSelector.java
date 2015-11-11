package com.sos.scheduler.plugins.monitor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sos.scheduler.InstallationService.batchInstallationModel.installations.Installation;
import sos.scheduler.InstallationService.batchInstallationModel.installations.Installations;

import com.sos.scheduler.model.objects.Job;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierFileFilter;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierFileSelector;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierFileSelectorOptions;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierJobFileFilter;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierMonitorFileFilter;
import com.sos.scheduler.plugins.globalmonitor.JobSchedulerFileElement;

public class TestConfigurationModifierFileSelector {

    private ConfigurationModifierFileSelector configurationModifierFileSelector;
    private ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions;
     
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
    public void testConfigurationModifierFileSelector() {
        configurationModifierFileSelectorOptions = new ConfigurationModifierFileSelectorOptions();

        configurationModifierFileSelectorOptions.setConfigurationDirectory("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_current/config");
        configurationModifierFileSelectorOptions.setDirectoryExclusions("/sos,test_event");
        configurationModifierFileSelectorOptions.setFileExclusions("/job_exercise1,/events2/ob_exercise3.job.xml");
        configurationModifierFileSelectorOptions.setRecursive(true);
        configurationModifierFileSelectorOptions.setRegexSelector("^Job_.*$");

        configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions);
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));

        configurationModifierFileSelector.fillSelectedFileList();

        
     }

    
    
    @Test
    public void testIsInJoblist() {
        configurationModifierFileSelectorOptions = new ConfigurationModifierFileSelectorOptions();

        configurationModifierFileSelectorOptions.setConfigurationDirectory("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_current/config");
        configurationModifierFileSelectorOptions.setDirectoryExclusions("/test_event");
        configurationModifierFileSelectorOptions.setFileExclusions("/job_exercise1,/events2/job_exercise3");
        configurationModifierFileSelectorOptions.setRecursive(true);
        configurationModifierFileSelectorOptions.setRegexSelector("^Job_.*$");

        configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions);
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));

        configurationModifierFileSelector.fillSelectedFileList();
        boolean b = configurationModifierFileSelector.isInSelectedFileList("/parallel_job_instances/Job_1");

        assertEquals("testIsInJoblist",true, b);
        configurationModifierFileSelectorOptions.setFileExclusions("/sos/housekeeping/job6,/events2/job_exercise3");
        configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions);
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));
        configurationModifierFileSelector.fillSelectedFileList();

         b = configurationModifierFileSelector.isInSelectedFileList("/sos/housekeeping/job6");
        assertEquals("testIsInJoblist",false, b);

     }    
    
    
    @Test
    public void testGetMonitorList() {
        configurationModifierFileSelectorOptions = new ConfigurationModifierFileSelectorOptions();

        configurationModifierFileSelectorOptions.setConfigurationDirectory("c:/temp");
        configurationModifierFileSelectorOptions.setDirectoryExclusions("/test_event");
        configurationModifierFileSelectorOptions.setFileExclusions("/job_exercise1,/events2/job_exercise3.job.xml");
        configurationModifierFileSelectorOptions.setRecursive(true);
        configurationModifierFileSelectorOptions.setRegexSelector("^job.*$");

        configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions);
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));
       

        configurationModifierFileSelector.fillSelectedFileList();
        boolean jobIsToBeHandled = configurationModifierFileSelector.isInSelectedFileList("/sos/housekeeping/job6");
        assertEquals("testGetMonitorList",true, jobIsToBeHandled);

         if (jobIsToBeHandled){
            
            JobSchedulerFileElement jobSchedulerFileElement= configurationModifierFileSelector.getJobSchedulerElement("/sos/housekeeping/job6");

            if (jobSchedulerFileElement != null){
                ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions2 = new ConfigurationModifierFileSelectorOptions();
                configurationModifierFileSelectorOptions2.setRegexSelector("^global_.*$");
                configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions2);
                configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierMonitorFileFilter(configurationModifierFileSelectorOptions2));

                configurationModifierFileSelector.fillParentMonitorList(jobSchedulerFileElement);
                
            }else{
                assertEquals("testGetMonitorList",true, false);
            }
        }
         

     }    
    
     
      
    
    
}
