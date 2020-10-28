package com.sos.scheduler.plugins.monitor;

import static org.junit.Assert.assertEquals;

import java.io.File;
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
    private static Runtime runtime = Runtime.getRuntime();
    
    public static void gc() {
        // Try to give the JVM some hints to run garbage collection
        for (int i = 0; i < 5; i++) {
            runtime.runFinalization();
            runtime.gc();
            Thread.currentThread().yield();
        }
    }


    @Test
    public void testJobChange() throws Exception, JAXBException {
        ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions = new ConfigurationModifierFileSelectorOptions();
        configurationModifierFileSelectorOptions.setConfigurationDirectory("scr/test/resources/config");
        configurationModifierFileSelectorOptions.setDirectoryExclusions("/sos");
        configurationModifierFileSelectorOptions.setFileExclusions("");
        configurationModifierFileSelectorOptions.setRecursive(true);
        configurationModifierFileSelectorOptions.setRegexSelector("^.*$");
        ConfigurationModifierFileSelector configurationModifierFileSelector = new ConfigurationModifierFileSelector(
                configurationModifierFileSelectorOptions);
        configurationModifierFileSelectorOptions.setConfigurationDirectory("src/test/resources/config");
      
        configurationModifierFileSelectorOptions.setDirectoryExclusions("/sos");
        configurationModifierFileSelectorOptions.setFileExclusions("");
        configurationModifierFileSelectorOptions.setRecursive(true);
        configurationModifierFileSelectorOptions.setRegexSelector("^.*$");
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));
        configurationModifierFileSelector.fillSelectedFileList();
        boolean jobIsToBeHandled = configurationModifierFileSelector.isInSelectedFileList("/myJob");
        assertEquals("testGetMonitorList", true, jobIsToBeHandled);
        if (jobIsToBeHandled) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder domBuilder = factory.newDocumentBuilder();
            Document doc = domBuilder.parse(new InputSource(new StringReader("<job></job>")));
            JobSchedulerFileElement jobSchedulerFileElement = configurationModifierFileSelector.getJobSchedulerElement(
                    "/myJob");
            if (jobSchedulerFileElement != null) {
                gc();
                ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions2 = new ConfigurationModifierFileSelectorOptions();
                configurationModifierFileSelectorOptions2.setRegexSelector("^global_monitor.*$");
                configurationModifierFileSelectorOptions2.setRecursive(true);
                configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions2);
                configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierMonitorFileFilter(
                        configurationModifierFileSelectorOptions2));
                configurationModifierFileSelector.fillParentMonitorList(jobSchedulerFileElement);
                System.out.println("Before generating JDOM objects: " + (runtime.totalMemory() - runtime.freeMemory()) + " bytes");

                JobConfigurationFileChanger jobConfigurationFileChanger = new JobConfigurationFileChanger(doc);
                jobConfigurationFileChanger.setListOfMonitors(configurationModifierFileSelector.getListOfMonitorConfigurationFiles());
                for (int i =0;i < 30;i++) {
                Long a =     runtime.totalMemory() - runtime.freeMemory();
                doc = jobConfigurationFileChanger.addMonitorUse();
                Long b =     runtime.totalMemory() - runtime.freeMemory();
                Long c = (b-a) / 1024;
                System.out.println("consumes " + c);
                
                 }
                System.out.println("After generating JDOM objects: " + (runtime.totalMemory() - runtime.freeMemory()) + " bytes");
                gc();
                System.out.println("GC After generating JDOM objects: " + (runtime.totalMemory() - runtime.freeMemory()) + " bytes");

            } else {
                assertEquals("testGetMonitorList", true, false);
            }
        }
    }

}