package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdShowJobChain extends ShowJobChain {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdShowJobChain";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdShowJobChain.class);

    public JSCmdShowJobChain(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }

}
