package com.sos.scheduler.model.objects;


import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjProcessClasses extends ProcessClasses {

    @SuppressWarnings("unused")
    private final String conClassName = "JSObjProcessClasses";

    public JSObjProcessClasses(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
