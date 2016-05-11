package com.sos.scheduler.plugins.monitor;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierFileSelector;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierFileSelectorOptions;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierJobFileFilter;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierMonitorFileFilter;
import com.sos.scheduler.plugins.globalmonitor.JobConfigurationFileChanger;
import com.sos.scheduler.plugins.globalmonitor.JobSchedulerFileElement;

public class TestJobConfigurationFileChanger {

    @Test
    public void testJobChange() throws Exception, JAXBException {
        // 1.Create and initialize the options object
        ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions = new ConfigurationModifierFileSelectorOptions();
        configurationModifierFileSelectorOptions.setConfigurationDirectory("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_current/config");
        configurationModifierFileSelectorOptions.setDirectoryExclusions("/sos");
        configurationModifierFileSelectorOptions.setFileExclusions("");
        configurationModifierFileSelectorOptions.setRecursive(true);
        configurationModifierFileSelectorOptions.setRegexSelector("^.*$");
        // 2. Create a FileSelector for the jobs that are to be handled
        // depending on the given options.
        ConfigurationModifierFileSelector configurationModifierFileSelector =
                new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions);
        // 3. Set the filter for jobs to an instance of
        // ConfigurationModifierJobFileFilter
        configurationModifierFileSelectorOptions.setConfigurationDirectory("C:/Users/ur/Documents/sos-berlin.com/jobscheduler/scheduler_current/config");
        configurationModifierFileSelectorOptions.setDirectoryExclusions("/sos");
        configurationModifierFileSelectorOptions.setFileExclusions("");
        configurationModifierFileSelectorOptions.setRecursive(true);
        configurationModifierFileSelectorOptions.setRegexSelector("^.*$");
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));
        // 4. getting the entire jobs
        configurationModifierFileSelector.fillSelectedFileList();
        boolean jobIsToBeHandled = configurationModifierFileSelector.isInSelectedFileList("/parallel_job_instances/Job_1");
        assertEquals("testGetMonitorList", true, jobIsToBeHandled);
        if (jobIsToBeHandled) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder domBuilder = factory.newDocumentBuilder();
            Document doc = domBuilder.parse(new InputSource(new StringReader("<job></job>")));
            // 5. if the current job is to be handled, create the list of
            // monitors to add.
            JobSchedulerFileElement jobSchedulerFileElement =
                    configurationModifierFileSelector.getJobSchedulerElement("/parallel_job_instances/Job_1");
            if (jobSchedulerFileElement != null) {
                // 6.Create and initialize the options object for the monitors.
                ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions2 = new ConfigurationModifierFileSelectorOptions();
                configurationModifierFileSelectorOptions2.setRegexSelector("^global_monitor.*$");
                configurationModifierFileSelectorOptions2.setRecursive(true);
                // 7. Create a FileSelector for the monitors that are to be
                // added to the monitor.use list depending on the given options.
                configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions2);
                // 8. Set the filter for jobs to an instance of
                // ConfigurationModifierMonitorFileFilter
                configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierMonitorFileFilter(
                        configurationModifierFileSelectorOptions2));
                configurationModifierFileSelector.fillParentMonitorList(jobSchedulerFileElement);
                // 9. Create a jobConfiguration changer to read, parse, change
                // and write the job.xml
                JobConfigurationFileChanger jobConfigurationFileChanger = new JobConfigurationFileChanger(doc);
                jobConfigurationFileChanger.setListOfMonitors(configurationModifierFileSelector.getListOfMonitorConfigurationFiles());
                doc = jobConfigurationFileChanger.addMonitorUse();
            } else {
                assertEquals("testGetMonitorList", true, false);
            }
        }
    }

}