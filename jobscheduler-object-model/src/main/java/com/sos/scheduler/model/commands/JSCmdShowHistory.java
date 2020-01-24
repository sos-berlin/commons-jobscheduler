package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdShowHistory extends ShowHistory {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdShowHistory";

    public JSCmdShowHistory(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

}
