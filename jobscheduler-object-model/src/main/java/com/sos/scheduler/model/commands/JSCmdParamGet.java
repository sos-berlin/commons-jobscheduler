package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdParamGet extends ParamGet {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdParamGet";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdParamGet.class);

    public JSCmdParamGet(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
