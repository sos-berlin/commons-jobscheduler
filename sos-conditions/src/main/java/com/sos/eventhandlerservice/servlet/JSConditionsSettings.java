package com.sos.eventhandlerservice.servlet;


import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSConditionsSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSConditionsSettings.class); 
    private Path hibernateConfigurationFile;
    private String propertiesFile;
    private String jobschedulerUrl;
    
    public String getPropertiesFile() {
        return propertiesFile;
    }

    
    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    public Path getHibernateConfigurationFile() {
        return hibernateConfigurationFile;
    }

    public void setHibernateConfigurationFile(Path hibernateConfigurationFile) {
        this.hibernateConfigurationFile = hibernateConfigurationFile;
    }

     
    public String getJobschedulerUrl() {
        return jobschedulerUrl;
    }

    
    public void setJobschedulerUrl(String jobschedulerUrl) {
        this.jobschedulerUrl = jobschedulerUrl;
    }

    
 
}
