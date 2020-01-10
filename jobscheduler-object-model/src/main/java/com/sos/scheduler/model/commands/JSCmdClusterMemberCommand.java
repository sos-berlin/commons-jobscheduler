package com.sos.scheduler.model.commands;


import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdClusterMemberCommand extends ClusterMemberCommand {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdClusterMemberCommand";

    public JSCmdClusterMemberCommand(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
