package com.sos.scheduler.plugins.monitor;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierFileSelector;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierFileSelectorOptions;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierJobFileFilter;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierMonitorFileFilter;
import com.sos.scheduler.plugins.globalmonitor.JobSchedulerFileElement;

public class TestConfigurationModifierFileSelector {

    private ConfigurationModifierFileSelector configurationModifierFileSelector;
    private ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions;

    @Before
    public void setUp() throws Exception {
        configurationModifierFileSelectorOptions = new ConfigurationModifierFileSelectorOptions();
        configurationModifierFileSelectorOptions.setConfigurationDirectory("src/test/resources/config");
        configurationModifierFileSelectorOptions.setDirectoryExclusions("/a");
        configurationModifierFileSelectorOptions.setFileExclusions("/a,/b.job.xml");
        configurationModifierFileSelectorOptions.setRegexSelector("^my.*$");
    }

    @Test
    public void testConfigurationModifierFileSelector() {
        configurationModifierFileSelectorOptions.setRecursive(true);
        configurationModifierFileSelectorOptions.setRegexSelector("^.*$");
        configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions);
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));
        configurationModifierFileSelector.fillSelectedFileList();
        assertEquals("testConfigurationModifierFileSelector", 2, configurationModifierFileSelector.getSelectedFileList().size());

    }

    @Test
    public void testIsInJoblist() {
        configurationModifierFileSelectorOptions.setRecursive(true);
        configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions);
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));
        configurationModifierFileSelector.fillSelectedFileList();
        boolean b = configurationModifierFileSelector.isInSelectedFileList("/myJob");
        assertEquals("testIsInJoblist", true, b);
        configurationModifierFileSelectorOptions.setFileExclusions("/sos/housekeeping/job6,/events2/job_exercise3");
        configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions);
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));
        configurationModifierFileSelector.fillSelectedFileList();
        b = configurationModifierFileSelector.isInSelectedFileList("/sos/housekeeping/job6");
        assertEquals("testIsInJoblist", false, b);
    }

    @Test
    public void testGetMonitorList() {
        configurationModifierFileSelectorOptions.setDirectoryExclusions("/test_event");
        configurationModifierFileSelectorOptions.setFileExclusions("/job_exercise1,/events2/job_exercise3.job.xml");
        configurationModifierFileSelectorOptions.setRecursive(true);
        configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions);
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));
        configurationModifierFileSelector.fillSelectedFileList();
        boolean jobIsToBeHandled = configurationModifierFileSelector.isInSelectedFileList("/myJob");
        assertEquals("testGetMonitorList", true, jobIsToBeHandled);
        if (jobIsToBeHandled) {
            JobSchedulerFileElement jobSchedulerFileElement = configurationModifierFileSelector.getJobSchedulerElement("/myJob");
            if (jobSchedulerFileElement != null) {
                ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions2 = new ConfigurationModifierFileSelectorOptions();
                configurationModifierFileSelectorOptions2.setRecursive(true);
                configurationModifierFileSelectorOptions2.setRegexSelector("^global_.*$");
                configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions2);
                configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierMonitorFileFilter(
                        configurationModifierFileSelectorOptions2));
                configurationModifierFileSelector.fillParentMonitorList(jobSchedulerFileElement);
            } else {
                assertEquals("testGetMonitorList", true, false);
            }
        }
    }

}