package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdShowJobChain extends ShowJobChain {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdShowJobChain";

    public JSCmdShowJobChain(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

}
