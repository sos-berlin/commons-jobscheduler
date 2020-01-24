package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdRemoteSchedulerRemoteTaskClose extends RemoteSchedulerRemoteTaskClose {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdRemoteSchedulerRemoteTaskClose";

    public JSCmdRemoteSchedulerRemoteTaskClose(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
