package com.sos.scheduler.plugins.globalmonitor;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import scala.collection.immutable.Set;

import com.sos.scheduler.engine.data.filebased.AbsolutePath;
import com.sos.scheduler.engine.data.filebased.FileBasedType;
import com.sos.scheduler.engine.kernel.plugin.AbstractPlugin;
import com.sos.scheduler.engine.kernel.plugin.Plugins;
import com.sos.scheduler.engine.kernel.plugin.XmlConfigurationChangingPlugin;

import static com.sos.scheduler.engine.common.xml.XmlUtils.loadXml;
import static com.sos.scheduler.engine.common.xml.XmlUtils.toXmlBytes;
import static com.sos.scheduler.engine.common.javautils.ScalaInJava.toScalaSet;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.nullToEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;

import javax.inject.Inject;
import javax.inject.Named;

import org.jdom.input.DOMBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class GlobalMonitorPlugin extends AbstractPlugin implements XmlConfigurationChangingPlugin  {
    
    private String paramConfigurationDirectory;
    private String paramDirectoryExclusions;
    private String paramFileExclusions;
    private String paramRecursiv;
    private String paramRegexSelector;
    private String paramMonitorRegexSelector;
    private HashMap<String, String> parameters;
    
    @Inject
    private GlobalMonitorPlugin(@Named(Plugins.configurationXMLName) Element pluginElement) {
      setParameters(pluginElement);
    }

    @Override
    public byte[] changeXmlConfiguration(FileBasedType typ, AbsolutePath path, byte[] xmlBytes) {
        Document doc=null;
        if (typ == FileBasedType.job){
            doc = xmlBytesToDom(xmlBytes);
            //modifyJobElement(doc.getDocumentElement());
        }
        try {
            doc = modifyJobElement(doc,path.withTrailingSlash());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return domToXmlBytes(doc);        
    }

    @Override
    public Set<FileBasedType> fileBasedTypes() {
        return toScalaSet(FileBasedType.job);
    }
    
    private void setParameters(Element e){
        parameters = new HashMap<String, String>();
        DOMBuilder domBuilder = new DOMBuilder();
        org.jdom.Element pluginElement = domBuilder.build(e);
        
        List <org.jdom.Element> listOfParams = null;

        org.jdom.Element  paramsElement = pluginElement.getChild("params");
        if (paramsElement != null) {
            listOfParams = paramsElement.getChildren("param");
         }
    
        Iterator<org.jdom.Element> it = listOfParams.iterator();
        while (it.hasNext()) {
            org.jdom.Element  param = it.next();
            if (param.getAttributeValue("name") != null) {
                String name = param.getAttributeValue("name").toLowerCase();
                String value =param.getAttributeValue("value");
                parameters.put(name,value);
                
             }
       }
        
        paramConfigurationDirectory =  parameters.get("configurationdirectory");
        paramDirectoryExclusions    =  parameters.get("directoryexclusions");
        paramFileExclusions         =  parameters.get("fileexclusions");
        paramRecursiv               =  parameters.get("recursiv");
        paramRegexSelector          =  parameters.get("regexselector");
        paramMonitorRegexSelector   =  parameters.get("monitorregexselector");
    }
                             
 
     
    private void modifyJobElementx(Element element) {
        checkArgument(element.getLocalName().equals("job"));
        String title = nullToEmpty(element.getAttribute("title"));
        element.setAttribute("title", title + " - " + "appendToTitle2" + paramConfigurationDirectory);
    }
    
    private static Document xmlBytesToDom(byte[] xmlBytes) {
        String encoding = "";
        return loadXml(xmlBytes, encoding);
    }
    
    private static byte[] domToXmlBytes(Node node) {
        boolean indent = false;
        return toXmlBytes(node, UTF_8, indent);
    }
    
    private Document modifyJobElement(Document doc, String jobname) throws FileNotFoundException, JAXBException, ParseException{
        //1.Create and initialize the options object
        ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions = new ConfigurationModifierFileSelectorOptions();
        
        configurationModifierFileSelectorOptions.setConfigurationDirectory(paramConfigurationDirectory);
        configurationModifierFileSelectorOptions.setDirectoryExclusions(paramDirectoryExclusions);
        configurationModifierFileSelectorOptions.setFileExclusions(paramFileExclusions);
        configurationModifierFileSelectorOptions.setRecursiv(paramRecursiv);
        configurationModifierFileSelectorOptions.setRegexSelector(paramRegexSelector);

        //2. Create a FileSelector for the jobs that are to be handled depending on the given options.
        ConfigurationModifierFileSelector configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions);
        
        //3. Set the filter for jobs to an instance of ConfigurationModifierJobFileFilter
        configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierJobFileFilter(configurationModifierFileSelectorOptions));
       

        //4. getting the entire jobs
        configurationModifierFileSelector.fillSelectedFileList();
        boolean jobIsToBeHandled = configurationModifierFileSelector.isInSelectedFileList(jobname);

         if (jobIsToBeHandled){
            
            //5. if the current job is to be handled, create the list of monitors to add. 
            JobSchedulerFileElement jobSchedulerFileElement= configurationModifierFileSelector.getJobSchedulerElement(jobname);

            if (jobSchedulerFileElement != null){//always will be != null, as this is the then part of jobIsToBeHandled-if
                //6.Create and initialize the options object for the monitors.
                ConfigurationModifierFileSelectorOptions configurationModifierFileSelectorOptions2 = new ConfigurationModifierFileSelectorOptions();
                                
                configurationModifierFileSelectorOptions2.setRegexSelector(paramMonitorRegexSelector);

                //7. Create a FileSelector for the monitors that are to be added to the monitor.use list depending on the given options.
                configurationModifierFileSelector = new ConfigurationModifierFileSelector(configurationModifierFileSelectorOptions2);
                
                //8. Set the filter for jobs to an instance of ConfigurationModifierMonitorFileFilter
                configurationModifierFileSelector.setSelectorFilter(new ConfigurationModifierMonitorFileFilter(configurationModifierFileSelectorOptions2));

                configurationModifierFileSelector.fillParentMonitorList(jobSchedulerFileElement);
              
                
                //9. Create a jobConfiguration changer to read, parse, change (and write) the job.xml
                JobConfigurationFileChanger jobConfigurationFileChanger = new JobConfigurationFileChanger(jobSchedulerFileElement);
                jobConfigurationFileChanger.setListOfMonitors(configurationModifierFileSelector.getListOfMonitorConfigurationFiles());
                
                jobConfigurationFileChanger.readConfigurationFile(doc);
                jobConfigurationFileChanger.changeConfigurationFile();
                return jobConfigurationFileChanger.getJobAsDocument();
            } 
         }
        return doc;
    }


}
