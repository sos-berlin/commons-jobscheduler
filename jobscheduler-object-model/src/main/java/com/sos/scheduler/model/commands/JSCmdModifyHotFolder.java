package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdModifyHotFolder extends ModifyHotFolder {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdModifyHotFolder";

    public JSCmdModifyHotFolder(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
