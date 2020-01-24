package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdParamGet extends ParamGet {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdParamGet";

    public JSCmdParamGet(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
