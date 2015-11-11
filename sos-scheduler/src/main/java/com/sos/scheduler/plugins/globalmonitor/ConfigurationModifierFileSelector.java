package com.sos.scheduler.plugins.globalmonitor;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;


public class ConfigurationModifierFileSelector {

    private static final Logger logger = Logger.getLogger(GlobalMonitorPlugin.class);

    private ConfigurationModifierFileSelectorOptions selectorOptions;
    private HashMap<String,JobSchedulerFileElement> listOfSelectedConfigurationFiles;
    private HashMap<String,JobSchedulerFileElement> listOfMonitorConfigurationFiles;
   

    public HashMap<String,JobSchedulerFileElement> getListOfMonitorConfigurationFiles() {
        return listOfMonitorConfigurationFiles;
    }

    private ConfigurationModifierFileFilter selectorFilter;
    
    public ConfigurationModifierFileSelector(ConfigurationModifierFileSelectorOptions selectorOptions_) {
        super();
        this.selectorOptions = selectorOptions_;
    }

    
    private void fillFiles(String directory){
       if (directory != null){
           File d = new File(directory);
           File[] files  = null;
           if (selectorFilter != null){
               files = d.listFiles(selectorFilter);
           }else{
               files = d.listFiles();
           }
        
           if (files != null){
               for(File file:files)  {
                   if (file.isFile()){
                       JobSchedulerFileElement jobSchedulerFileElement = new JobSchedulerFileElement(file);
                       listOfSelectedConfigurationFiles.put(jobSchedulerFileElement.getJobSchedulerElementName(),jobSchedulerFileElement);
                   }else{
                       if (selectorOptions.isRecursive()){
                           fillFiles(file.getAbsolutePath());
                       }
                   }
               }   
           }
       }
    }
    
    public void fillSelectedFileList(){      
        listOfSelectedConfigurationFiles = new HashMap<String,JobSchedulerFileElement> ();
        fillFiles(selectorOptions.getConfigurationDirectory()+"/cache");
        fillFiles(selectorOptions.getConfigurationDirectory()+"/live");    

    }
    
    public HashMap<String,JobSchedulerFileElement>  getSelectedFileList(){
        return listOfSelectedConfigurationFiles;
    }
    
    public boolean isInSelectedFileList(String elementName){
    	return (listOfSelectedConfigurationFiles.get(elementName) != null);
    }

    public JobSchedulerFileElement getJobSchedulerElement(String jobName){
    	return listOfSelectedConfigurationFiles.get(jobName);
    }
    
    private void fillMonitorList(File d, String schedulerLivePath){

    	if (d != null) {
     		
         if (selectorOptions.isRecursive()){
             if (d != null && d.getAbsolutePath().length()> 0 && !d.getAbsolutePath().replace('\\', '/').equals(schedulerLivePath)){
               fillMonitorList(d.getParentFile(),schedulerLivePath);
            }
        }
        File[] files= null;

        if (selectorFilter != null){
        	files = d.listFiles(selectorFilter);
        	
        }else{
           files = d.listFiles();
        }
        if (files != null){
            for(File file:files)  {
                if (file.isFile()){
                    JobSchedulerFileElement jobSchedulerFileElement = new JobSchedulerFileElement(file);
                    listOfMonitorConfigurationFiles.put(jobSchedulerFileElement.getJobSchedulerElementName(),jobSchedulerFileElement);
                }
            }
        }
    	}
      }
    
    private void fillParentMonitorListFromBase(JobSchedulerFileElement jobSchedulerFileElement, String base){
    	   
        String schedulerLivePath = jobSchedulerFileElement.getSchedulerLivePath();
        File d = jobSchedulerFileElement.getConfigurationFile();
        String s = d.getAbsolutePath();
        s = s.replace("\\", "/");
        if (base.equals("live")){
            s = s.replaceFirst("/cache", "/" + base);
        }else{
            s = s.replaceFirst("/live", "/" + base);
        }
        File dLive = new File(s);
        fillMonitorList(dLive,schedulerLivePath+"/cache");
    }
    
    public void fillParentMonitorList(JobSchedulerFileElement jobSchedulerFileElement){
        listOfMonitorConfigurationFiles = new HashMap<String,JobSchedulerFileElement>();
        
        fillParentMonitorListFromBase(jobSchedulerFileElement,"live");
        fillParentMonitorListFromBase(jobSchedulerFileElement,"cache");
    }

    public void setSelectorFilter(ConfigurationModifierFileFilter selectorFilter) {
        this.selectorFilter = selectorFilter;
    }
}
