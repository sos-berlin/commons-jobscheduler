package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

 
public class JSCmdEventsGet extends EventsGet {

	@SuppressWarnings("unused")
	private final String		conClassName	= "JSCmdEventsGet";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdEventsGet.class);

	public JSCmdEventsGet (SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}
}
