package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

 
public class JSCmdRemoveJobChain extends RemoveJobChain {

	@SuppressWarnings("unused")
	private final String		conClassName	= "JSCmdRemoveJobChain";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdRemoveJobChain.class);

	public JSCmdRemoveJobChain (SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}
}
