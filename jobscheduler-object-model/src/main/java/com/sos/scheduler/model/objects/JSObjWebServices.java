package com.sos.scheduler.model.objects;

import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjWebServices extends WebServices {

    @SuppressWarnings("unused")
    private final String conClassName = "JSObjWebServices";

    public JSObjWebServices(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
