package com.sos.scheduler.model.objects;


import com.sos.scheduler.model.SchedulerObjectFactory;


/** @author oh */
public class JSObjConfigurationDirectory extends ConfigurationDirectory {

    @SuppressWarnings("unused")
    private final String conClassName = "JSObjConfigurationDirectory";

    public JSObjConfigurationDirectory(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
