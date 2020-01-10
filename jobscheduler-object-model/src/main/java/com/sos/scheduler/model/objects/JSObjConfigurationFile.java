package com.sos.scheduler.model.objects;

import com.sos.scheduler.model.SchedulerObjectFactory;


/** @author oh */
public class JSObjConfigurationFile extends ConfigurationFile {

    @SuppressWarnings("unused")
    private final String conClassName = "JSObjConfigurationFile";

    public JSObjConfigurationFile(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
