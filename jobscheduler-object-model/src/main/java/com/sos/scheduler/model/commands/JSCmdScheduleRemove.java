package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

 
public class JSCmdScheduleRemove extends ScheduleRemove {

	@SuppressWarnings("unused")
	private final String		conClassName	= "JSCmdScheduleRemove";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdScheduleRemove.class);

	public JSCmdScheduleRemove (SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}
}
