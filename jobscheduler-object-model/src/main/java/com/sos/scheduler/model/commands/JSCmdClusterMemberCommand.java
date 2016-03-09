package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdClusterMemberCommand extends ClusterMemberCommand {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdClusterMemberCommand";
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JSCmdClusterMemberCommand.class);

    public JSCmdClusterMemberCommand(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
