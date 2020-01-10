package com.sos.scheduler.model.objects;

import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjRegisterRemoteScheduler extends RegisterRemoteScheduler {

    @SuppressWarnings("unused")
    private final String conClassName = "JSObjRegisterRemoteScheduler";

    public JSObjRegisterRemoteScheduler(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
