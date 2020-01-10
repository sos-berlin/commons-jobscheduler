package com.sos.scheduler.model.objects;

import com.sos.scheduler.model.SchedulerObjectFactory;

/** @author oh */
public class JSObjNote extends Note {

    public JSObjNote(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
