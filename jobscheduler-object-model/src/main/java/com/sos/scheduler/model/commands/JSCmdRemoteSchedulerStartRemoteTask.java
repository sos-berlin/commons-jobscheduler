package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdRemoteSchedulerStartRemoteTask extends RemoteSchedulerStartRemoteTask {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdRemoteSchedulerStartRemoteTask";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdRemoteSchedulerStartRemoteTask.class);

    public JSCmdRemoteSchedulerStartRemoteTask(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
