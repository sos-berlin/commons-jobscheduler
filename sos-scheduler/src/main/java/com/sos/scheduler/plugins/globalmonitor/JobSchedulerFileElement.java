package com.sos.scheduler.plugins.globalmonitor;

import java.io.File;

public class JobSchedulerFileElement {
    private File configurationFile;
    private String jobSchedulerElementName;
    private String schedulerLivePath="";
 
    
    
    public JobSchedulerFileElement(File configurationFile, String schedulerLivePath_) {
        this.configurationFile = configurationFile;
        this.schedulerLivePath = schedulerLivePath_;
        setJobSchedulerElementName();
    }

    private String getSchedulerLivePath(String filePath){
        if (schedulerLivePath.equals("")){
            filePath = filePath.replace('\\','/');
            String s = filePath.replaceFirst("^(.*/live)/.*","$1");
            return s;
        }else{
            return schedulerLivePath;
        }
    }

    public String getSchedulerLivePath(){
        return getSchedulerLivePath(configurationFile.getAbsolutePath());
    }

    public File getConfigurationFile() {
        return configurationFile;
    }
     

    public String getJobSchedulerElementName() {
        return jobSchedulerElementName;
    }
 
    private void setJobSchedulerElementName(){
        String filePath = configurationFile.getAbsolutePath();
        filePath = filePath.replace('\\', '/');
        jobSchedulerElementName = filePath;
        if (filePath.startsWith(getSchedulerLivePath(filePath))){
            int l = getSchedulerLivePath(filePath).length();
            jobSchedulerElementName = filePath.substring(l);
        }
        jobSchedulerElementName = jobSchedulerElementName.replace('\\', '/');
        jobSchedulerElementName = jobSchedulerElementName.replaceAll("(^/.*)\\..*\\.xml$", "$1");
    }
    
    public String testGetSchedulerHome(String filePath){
        return getSchedulerLivePath(filePath);
    }
}
