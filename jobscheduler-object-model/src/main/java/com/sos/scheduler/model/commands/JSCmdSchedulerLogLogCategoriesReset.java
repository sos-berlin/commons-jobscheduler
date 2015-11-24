package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

 
public class JSCmdSchedulerLogLogCategoriesReset extends SchedulerLogLogCategoriesReset {

	@SuppressWarnings("unused")
	private final String		conClassName	= "JSCmdSchedulerLogLogCategoriesReset";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdSchedulerLogLogCategoriesReset.class);

	public JSCmdSchedulerLogLogCategoriesReset (SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}
}
