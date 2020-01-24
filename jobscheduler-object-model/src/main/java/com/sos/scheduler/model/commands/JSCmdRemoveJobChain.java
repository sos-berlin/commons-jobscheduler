package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdRemoveJobChain extends RemoveJobChain {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdRemoveJobChain";

    public JSCmdRemoveJobChain(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
