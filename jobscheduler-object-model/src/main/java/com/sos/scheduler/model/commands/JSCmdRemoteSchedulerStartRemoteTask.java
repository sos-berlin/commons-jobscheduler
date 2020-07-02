package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdRemoteSchedulerStartRemoteTask extends RemoteSchedulerStartRemoteTask {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdRemoteSchedulerStartRemoteTask";

    public JSCmdRemoteSchedulerStartRemoteTask(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
