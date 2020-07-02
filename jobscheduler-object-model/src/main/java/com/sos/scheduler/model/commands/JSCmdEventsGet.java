package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdEventsGet extends EventsGet {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdEventsGet";

    public JSCmdEventsGet(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
