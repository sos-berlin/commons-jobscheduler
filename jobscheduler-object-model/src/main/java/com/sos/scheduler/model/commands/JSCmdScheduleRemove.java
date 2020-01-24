package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdScheduleRemove extends ScheduleRemove {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdScheduleRemove";

    public JSCmdScheduleRemove(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
