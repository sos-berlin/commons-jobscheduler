package com.sos.auth.classes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

 
public class SOSShiroProperties {
    private static final Logger LOGGER = Logger.getLogger(SOSShiroProperties.class);
    private Properties properties;
    private String propertiesFile;

    public SOSShiroProperties()  {
        super();
        propertiesFile = "/joc.properties";
        properties = new Properties();
        getProperties();
    }

    private void getProperties()   {
        final InputStream stream = this.getClass().getResourceAsStream(propertiesFile);

        if (stream != null) {
            try {
                properties.load(stream);
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        } 
 
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
        getProperties();
    }
    
    public String getProperty(String property){
       return properties.getProperty(property);
    }

}
