package com.sos.scheduler.plugins.globalmonitor;

import static com.sos.scheduler.engine.common.javautils.ScalaInJava.toScalaSet;
import static com.sos.scheduler.engine.common.xml.XmlUtils.loadXml;
import static com.sos.scheduler.engine.common.xml.XmlUtils.toXmlBytes;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.StringWriter;
import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sos.scheduler.engine.data.filebased.AbsolutePath;
import com.sos.scheduler.engine.data.filebased.FileBasedType;
import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.plugin.Plugins;
import com.sos.scheduler.engine.kernel.plugin.XmlConfigurationChangingPlugin;

import scala.collection.immutable.Set;

public class GlobalMonitorPlugin extends AbstractPlugin implements XmlConfigurationChangingPlugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalMonitorPlugin.class);
    ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorJobOptions;
    ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorMonitorOptions;
    private HashMap<String, String> parameters;

    @Inject
    private GlobalMonitorPlugin(@Named(Plugins.configurationXMLName) Element pluginElement) {
        configurationModifierFileSelectorJobOptions = setParameters(pluginElement, "jobparams");
        configurationModifierFileSelectorMonitorOptions = setParameters(pluginElement, "monitorparams");
    }

    public GlobalMonitorPlugin() {
     }

    @Override
    public byte[] changeXmlConfiguration(FileBasedType typ, AbsolutePath path, byte[] xmlBytes) {
        LOGGER.debug("---------  changeXmlConfiguration");
        Document doc = null;
        if (typ == FileBasedType.Job) {
            doc = xmlBytesToDom(xmlBytes);
        }

        doc = modifyJobElement(doc, path.string());

        return domToXmlBytes(doc);
    }

    @Override
    public Set<FileBasedType> fileBasedTypes() {
        return toScalaSet(FileBasedType.Job);
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

        NodeList listOfParams = null;
        NodeList paramsElement = e.getElementsByTagName(parent);
        if (paramsElement != null) {
            listOfParams = paramsElement.item(0).getChildNodes();
            for (int i = 0; i < listOfParams.getLength(); i++) {
                Node param = listOfParams.item(i);
                if (param.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) param;

                    if (elem.getAttribute("name") != null) {
                        String name = elem.getAttribute("name").toLowerCase();
                        String value = elem.getAttribute("value");
                        LOGGER.debug("---------  " + name + "=" + value);
                        parameters.put(name, value);
                    }
                }
            }
            c.setConfigurationDirectory(parameters.get("configuration_directory").replace('\\', '/'));
            c.setDirectoryExclusions(parameters.get("exclude_dir").replace('\\', '/'));
            c.setFileExclusions(parameters.get("exclude_file").replace('\\', '/'));
            c.setRecursive(parameters.get("recursive"));
            c.setRegexSelector(parameters.get("regex_selector"));
        }
        return c;
    }

    public ConfigurationModifierFileSelectorOptions setParametersTest(Element e, String parent) {
        return setParameters(e, parent);
    }

    private static Document xmlBytesToDom(byte[] xmlBytes) {
        String encoding = "";
        return loadXml(xmlBytes, encoding);
    }

    private static byte[] domToXmlBytes(Node node) {
        boolean indent = false;
        return toXmlBytes(node, UTF_8, indent);
    }

    private Document modifyJobElement(Document doc, String jobname) {
        LOGGER.debug("---------  modifyJobElement:" + jobname);
        // 1. Create a FileSelector for the jobs that are to be handled
        // depending on the given options.
        ConfigurationModifierFileSelector configurationModifierFileSelector = new ConfigurationModifierFileSelector(
                configurationModifierFileSelectorJobOptions);
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
                configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierMonitorFileFilter(
                        configurationModifierFileSelectorMonitorOptions));
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
