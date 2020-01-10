package com.sos.scheduler.model.objects;


import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjEnvironment extends Environment {

    @SuppressWarnings("unused")
    private final String conClassName = "JSObjEnvironment";

    public JSObjEnvironment(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
