package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdRemoteSchedulerRemoteTaskClose extends RemoteSchedulerRemoteTaskClose {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdRemoteSchedulerRemoteTaskClose";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdRemoteSchedulerRemoteTaskClose.class);

    public JSCmdRemoteSchedulerRemoteTaskClose(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
