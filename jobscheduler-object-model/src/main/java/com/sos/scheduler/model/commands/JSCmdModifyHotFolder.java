package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdModifyHotFolder extends ModifyHotFolder {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdModifyHotFolder";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdModifyHotFolder.class);

    public JSCmdModifyHotFolder(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
