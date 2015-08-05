package com.sos.scheduler.plugins.globalmonitor;

import java.io.File;
import java.util.ArrayList;

import org.apache.log4j.Logger;


public class ConfigurationModifierFileSelector {

    private static final Logger logger = Logger.getLogger(GlobalMonitorPlugin.class);

    private ConfigurationModifierFileSelectorOptions selectorOptions;
    private ArrayList<JobSchedulerFileElement> listOfSelectedConfigurationFiles;
    private ArrayList<JobSchedulerFileElement> listOfMonitorConfigurationFiles;
   

    public ArrayList<JobSchedulerFileElement> getListOfMonitorConfigurationFiles() {
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
                       logger.debug(file.getAbsolutePath() + " added");
                       JobSchedulerFileElement jobSchedulerFileElement = new JobSchedulerFileElement(file);
                       listOfSelectedConfigurationFiles.add(jobSchedulerFileElement);
                   }else{
                       if (selectorOptions.isRecursive()){
                           logger.debug("reading " + file.getAbsolutePath());
                           fillFiles(file.getAbsolutePath());
                       }
                   }
               }   
           }
       }
    }
    
    public void fillSelectedFileList(){      
        listOfSelectedConfigurationFiles = new ArrayList<JobSchedulerFileElement> ();
        fillFiles(selectorOptions.getConfigurationDirectory()+"/cache");
        fillFiles(selectorOptions.getConfigurationDirectory()+"/live");    

    }
    
    public ArrayList<JobSchedulerFileElement> getSelectedFileList(){
        return listOfSelectedConfigurationFiles;
    }
    
    public boolean isInSelectedFileList(String elementName){
        for(JobSchedulerFileElement jobElement:listOfSelectedConfigurationFiles)  {
            logger.debug("isinSelectedFileList: " + elementName + "=?" + jobElement.getJobSchedulerElementName());

            if (elementName.equals(jobElement.getJobSchedulerElementName())){
                return true;
            }
        }
        return false;
        
    }

    public JobSchedulerFileElement getJobSchedulerElement(String jobName){
        for(JobSchedulerFileElement jobSchedulerElement:listOfSelectedConfigurationFiles)  {
            if (jobName.equals(jobSchedulerElement.getJobSchedulerElementName())){
                return jobSchedulerElement;
            }
        }
        return null;
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
                	logger.debug(file.getAbsolutePath() + " -------------- monitor added");
                    JobSchedulerFileElement jobSchedulerFileElement = new JobSchedulerFileElement(file);
                    listOfMonitorConfigurationFiles.add(jobSchedulerFileElement);
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
        listOfMonitorConfigurationFiles = new ArrayList<JobSchedulerFileElement>();
        
        fillParentMonitorListFromBase(jobSchedulerFileElement,"live");
        fillParentMonitorListFromBase(jobSchedulerFileElement,"cache");
        
        
         
    }

    public void setSelectorFilter(ConfigurationModifierFileFilter selectorFilter) {
        this.selectorFilter = selectorFilter;
    }
}
