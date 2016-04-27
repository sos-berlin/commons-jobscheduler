package com.sos.scheduler.plugins.globalmonitor;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import scala.collection.immutable.Set;

import com.sos.scheduler.engine.data.filebased.AbsolutePath;
import com.sos.scheduler.engine.data.filebased.FileBasedType;
import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.plugin.Plugins;
import com.sos.scheduler.engine.kernel.plugin.XmlConfigurationChangingPlugin;

import static com.sos.scheduler.engine.common.xml.XmlUtils.loadXml;
import static com.sos.scheduler.engine.common.xml.XmlUtils.toXmlBytes;
import static com.sos.scheduler.engine.common.javautils.ScalaInJava.toScalaSet;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;
import org.jdom.JDOMException;
import org.jdom.input.DOMBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class GlobalMonitorPlugin extends AbstractPlugin implements XmlConfigurationChangingPlugin {

    private static final Logger LOGGER = Logger.getLogger(GlobalMonitorPlugin.class);
    ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorJobOptions;
    ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorMonitorOptions;
    private HashMap<String, String> parameters;

    @Inject
    private GlobalMonitorPlugin(@Named(Plugins.configurationXMLName) Element pluginElement) {
        configurationModifierFileSelectorJobOptions = setParameters(pluginElement, "jobparams");
        configurationModifierFileSelectorMonitorOptions = setParameters(pluginElement, "monitorparams");
    }

    @Override
    public byte[] changeXmlConfiguration(FileBasedType typ, AbsolutePath path, byte[] xmlBytes) {
        LOGGER.debug("---------  changeXmlConfiguration");
        Document doc = null;
        if (typ == FileBasedType.job) {
            doc = xmlBytesToDom(xmlBytes);
        }
        try {
            doc = modifyJobElement(doc, path.string());
        } catch (JDOMException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return domToXmlBytes(doc);
    }

    @Override
    public Set<FileBasedType> fileBasedTypes() {
        return toScalaSet(FileBasedType.job);
    }

    private String getStringFromDocument(Document doc) {
        try {
            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);
            return writer.toString();
        } catch (TransformerException ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }

    private ConfigurationModifierFileSelectorOptions setParameters(Element e, String parent) {
        LOGGER.debug("---------  setParameters:" + parent);
        ConfigurationModifierFileSelectorOptions c = new ConfigurationModifierFileSelectorOptions();
        parameters = new HashMap<String, String>();
        parameters.put("configuration_directory", "");
        parameters.put("exclude_dir", "");
        parameters.put("exclude_file", "");
        parameters.put("recursive", "true");
        parameters.put("regex_selector", "");
        DOMBuilder domBuilder = new DOMBuilder();
        org.jdom.Element pluginElement = domBuilder.build(e);
        List<org.jdom.Element> listOfParams = null;
        org.jdom.Element paramsElement = pluginElement.getChild(parent);
        if (paramsElement != null) {
            listOfParams = paramsElement.getChildren("param");
            Iterator<org.jdom.Element> it = listOfParams.iterator();
            while (it.hasNext()) {
                org.jdom.Element param = it.next();
                if (param.getAttributeValue("name") != null) {
                    String name = param.getAttributeValue("name").toLowerCase();
                    String value = param.getAttributeValue("value");
                    LOGGER.debug("---------  " + name + "=" + value);
                    parameters.put(name, value);
                }
            }
            c.setConfigurationDirectory(parameters.get("configuration_directory"));
            c.setDirectoryExclusions(parameters.get("exclude_dir"));
            c.setFileExclusions(parameters.get("exclude_file"));
            c.setRecursive(parameters.get("recursive"));
            c.setRegexSelector(parameters.get("regex_selector"));
        }
        return c;
    }

    private static Document xmlBytesToDom(byte[] xmlBytes) {
        String encoding = "";
        return loadXml(xmlBytes, encoding);
    }

    private static byte[] domToXmlBytes(Node node) {
        boolean indent = false;
        return toXmlBytes(node, UTF_8, indent);
    }

    private Document modifyJobElement(Document doc, String jobname) throws JDOMException {
        LOGGER.debug("---------  modifyJobElement:" + jobname);
        // 1. Create a FileSelector for the jobs that are to be handled
        // depending on the given options.
        ConfigurationModifierFileSelector configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorJobOptions);
        // 2. Set the filter for jobs to an instance of
        // ConfigurationModifierJobFileFilter
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorJobOptions));
        // 3. getting the entire jobs
        LOGGER.debug("---------  fillSelectedFileList");
        configurationModifierFileSelector.fillSelectedFileList();
        boolean jobIsToBeHandled = configurationModifierFileSelector.isInSelectedFileList(jobname);
        LOGGER.debug("---------  jobIsToBeHandled:" + jobIsToBeHandled);
        if (jobIsToBeHandled) {
            // 4. if the current job is to be handled, create the list of
            // monitors to add.
            JobSchedulerFileElement jobSchedulerFileElement = configurationModifierFileSelector.getJobSchedulerElement(jobname);
            if (jobSchedulerFileElement != null) {
                // always will be != null, as this is the then part of
                // jobIsToBeHandled-if
                // 5. Create a FileSelector for the monitors that are to be
                // added to the monitor.use list depending on the given options.
                configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorMonitorOptions);
                // 6. Set the filter for jobs to an instance of
                // ConfigurationModifierMonitorFileFilter
                configurationModifierFileSelector
                        .setSelectorFilter(new ConfigurationModifierMonitorFileFilter(configurationModifierFileSelectorMonitorOptions));
                configurationModifierFileSelector.fillParentMonitorList(jobSchedulerFileElement);
                // 7. Create a jobConfiguration changer to read, parse, change
                // (and write) the job.xml
                JobConfigurationFileChanger jobConfigurationFileChanger = new JobConfigurationFileChanger(doc);
                jobConfigurationFileChanger.setListOfMonitors(configurationModifierFileSelector.getListOfMonitorConfigurationFiles());
                doc = jobConfigurationFileChanger.addMonitorUse();
                LOGGER.debug(getStringFromDocument(doc));
                return doc;
            }
        }
        return doc;
    }

}
