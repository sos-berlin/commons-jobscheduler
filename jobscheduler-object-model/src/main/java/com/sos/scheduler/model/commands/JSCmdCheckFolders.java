package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdCheckFolders extends CheckFolders {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdCheckFolders";

    public JSCmdCheckFolders(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
