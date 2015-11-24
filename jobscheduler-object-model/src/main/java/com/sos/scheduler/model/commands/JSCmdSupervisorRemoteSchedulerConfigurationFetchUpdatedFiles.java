package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

 
public class JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles extends SupervisorRemoteSchedulerConfigurationFetchUpdatedFiles {

	@SuppressWarnings("unused")
	private final String		conClassName	= "JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles.class);

	public JSCmdSupervisorRemoteSchedulerConfigurationFetchUpdatedFiles (SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}
}
