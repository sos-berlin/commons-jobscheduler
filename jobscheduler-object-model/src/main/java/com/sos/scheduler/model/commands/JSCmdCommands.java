package com.sos.scheduler.model.commands;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjCommands;
import org.apache.log4j.Logger;

 
public class JSCmdCommands extends JSObjCommands {

	@SuppressWarnings("unused")
	private final String		conClassName	= "JSCmdCommands";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdCommands.class);

	public JSCmdCommands(SchedulerObjectFactory schedulerObjectFactory) {
		super(schedulerObjectFactory);
	}

}
