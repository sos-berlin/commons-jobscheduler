package com.sos.scheduler.plugins.monitor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sos.scheduler.engine.data.filebased.AbsolutePath;
import com.sos.scheduler.engine.data.filebased.FileBasedType;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierFileSelector;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierFileSelectorOptions;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierJobFileFilter;
import com.sos.scheduler.plugins.globalmonitor.ConfigurationModifierMonitorFileFilter;
import com.sos.scheduler.plugins.globalmonitor.GlobalMonitorPlugin;
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

    private void logXml(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String output = writer.getBuffer().toString().replaceAll("\n|\r", "");
        System.out.println(output);
    }

    @Test
    public void testXmlChange() throws SAXException, IOException, ParserConfigurationException, TransformerException {

        Map<String, JobSchedulerFileElement> listOfMonitors = new HashMap<String, JobSchedulerFileElement>();
        listOfMonitors.put("test22", null);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(new File("src/test/resources/config/live", "myJob.job.xml"));
        logXml(doc);

        JobConfigurationFileChanger jobConfigurationFileChanger = new JobConfigurationFileChanger(doc);
        jobConfigurationFileChanger.setListOfMonitors(listOfMonitors);

        doc = jobConfigurationFileChanger.addMonitorUse();
        logXml(doc);
    }

    @Test
    public void testParameterSetting() throws SAXException, IOException, ParserConfigurationException, TransformerException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        DocumentBuilder db = dbf.newDocumentBuilder();

        Document doc = db.parse(new File("src/test/resources/config/", "plugin.xml"));

        GlobalMonitorPlugin globalMonitorPlugin = new GlobalMonitorPlugin();
        globalMonitorPlugin.setParametersTest(doc.getDocumentElement(), "jobparams");

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
            JobSchedulerFileElement jobSchedulerFileElement = configurationModifierFileSelector.getJobSchedulerElement("/myJob");
            if (jobSchedulerFileElement != null) {
                gc();
                ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions2 = new ConfigurationModifierFileSelectorOptions();
                configurationModifierFileSelectorOptions2.setRegexSelector("^global_monitor.*$");
                configurationModifierFileSelectorOptions2.setRecursive(true);
                configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions2);
                configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierMonitorFileFilter(
                        configurationModifierFileSelectorOptions2));
                configurationModifierFileSelector.fillParentMonitorList(jobSchedulerFileElement);
                System.out.println("Before generating XML objects: " + (runtime.totalMemory() - runtime.freeMemory()) + " bytes");

                JobConfigurationFileChanger jobConfigurationFileChanger = new JobConfigurationFileChanger(doc);
                jobConfigurationFileChanger.setListOfMonitors(configurationModifierFileSelector.getListOfMonitorConfigurationFiles());
                for (int i = 0; i < 30; i++) {
                    Long a = runtime.totalMemory() - runtime.freeMemory();
                    doc = jobConfigurationFileChanger.addMonitorUse();
                    Long b = runtime.totalMemory() - runtime.freeMemory();
                    Long c = (b - a) / 1024;
                    System.out.println("consumes " + c);

                }
                System.out.println("After generating XML objects: " + (runtime.totalMemory() - runtime.freeMemory()) + " bytes");
                gc();
                System.out.println("GC After generating XML objects: " + (runtime.totalMemory() - runtime.freeMemory()) + " bytes");

            } else {
                assertEquals("testGetMonitorList", true, false);
            }
        }
    }

}