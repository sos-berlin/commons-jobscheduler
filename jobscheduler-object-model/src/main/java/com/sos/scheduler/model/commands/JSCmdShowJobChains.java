package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

 

public class JSCmdShowJobChains extends ShowJobChains {

	@SuppressWarnings("unused")
	private final String		conClassName	= "JSCmdShowJobChains";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdShowJobChains.class);

	public JSCmdShowJobChains(SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}
 
}
