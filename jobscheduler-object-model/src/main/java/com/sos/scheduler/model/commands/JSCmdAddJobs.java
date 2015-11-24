package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.objects.JSObjJob;


public class JSCmdAddJobs extends AddJobs {

	private final String		conClassName	= "JSCmdAddJobs";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdAddJobs.class);


	public JSCmdAddJobs(SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}
	
	public void add(JSObjJob pobjJob) {
	
	@SuppressWarnings("unused")
	final String	conMethodName	= conClassName + "::add";
	
	this.getJob().add(pobjJob);
	
} // private void add

}
