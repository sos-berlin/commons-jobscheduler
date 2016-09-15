package com.sos.auth.classes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import sos.scheduler.misc.ParameterSubstitutor;

public class SOSShiroProperties {
    private static final Logger LOGGER = Logger.getLogger(SOSShiroProperties.class);
    private Properties properties;
    private String propertiesFile;
    private ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();

    public SOSShiroProperties() {
        super();
        propertiesFile = "/joc.properties";
        properties = new Properties();
        readProperties();
    }

    private void substituteProperties() {
        parameterSubstitutor = new ParameterSubstitutor();
        for (Map.Entry<Object, Object> e : properties.entrySet()) {
            String key = (String) e.getKey();
            String value = (String) e.getValue();
            parameterSubstitutor.addKey(key, value);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    private void readProperties() {
        final InputStream stream = this.getClass().getResourceAsStream(propertiesFile);

        if (stream != null) {
            try {
                properties.load(stream);
                substituteProperties();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public void setPropertiesFile(String propertiesFile) {
        this.propertiesFile = propertiesFile;
        readProperties();
    }

    public String getProperty(String property) {
        String s = properties.getProperty(property);
        if (s != null){
            s = parameterSubstitutor.replaceEnvVars(s);
            s = parameterSubstitutor.replace(s);
        }
        return s;
    }

}
