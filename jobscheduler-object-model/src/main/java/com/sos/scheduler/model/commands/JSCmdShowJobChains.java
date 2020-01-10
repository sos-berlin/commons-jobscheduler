package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdShowJobChains extends ShowJobChains {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdShowJobChains";

    public JSCmdShowJobChains(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

}
