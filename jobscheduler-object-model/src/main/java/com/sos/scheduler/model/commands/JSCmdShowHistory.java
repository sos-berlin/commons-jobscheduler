package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

 

public class JSCmdShowHistory extends ShowHistory {

	@SuppressWarnings("unused")
	private final String		conClassName	= "JSCmdShowHistory";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdShowHistory.class);

	public JSCmdShowHistory(SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}
 
}
