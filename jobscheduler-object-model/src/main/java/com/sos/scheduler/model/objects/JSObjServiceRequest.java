package com.sos.scheduler.model.objects;

import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjServiceRequest extends ServiceRequest {

    public JSObjServiceRequest(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
