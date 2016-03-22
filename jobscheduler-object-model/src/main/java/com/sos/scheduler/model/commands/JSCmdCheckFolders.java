package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdCheckFolders extends CheckFolders {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdCheckFolders";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdCheckFolders.class);

    public JSCmdCheckFolders(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
