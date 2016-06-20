package com.sos.scheduler.plugins.variablehandling;

import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import sos.scheduler.misc.ParameterSubstitutor;

public class JobSchedulerParameterSubstitution {
    
    private HashMap<String,String> parameters;
    private static final Logger LOGGER = Logger.getLogger(JobSchedulerParameterSubstitution.class);

    private void replace(){
        ParameterSubstitutor parameterSubstitutor = new ParameterSubstitutor();       
        
        for (Entry<String, String> entry : parameters.entrySet()) {
            String value = entry.getValue();
            String paramName = entry.getKey().toUpperCase();
            LOGGER.debug("---->" + paramName + "=" + value);
            if (!value.isEmpty()) {
                parameterSubstitutor.addKey(paramName, value);
            }
        }

        String s = parameterSubstitutor.replace("");
       
    }

}
