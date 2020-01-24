package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;

public class JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles extends SupervisorRemoteSchedulerConfigurationFetchUpdatedFiles {

    @SuppressWarnings("unused")
    private final String conClassName = "JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles";

    public JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles(SchedulerObjectFactory schedulerObjectFactory) {
        super();
        objFactory = schedulerObjectFactory;
    }
}
