package com.sos.scheduler.model.commands;

import org.apache.log4j.Logger;

import com.sos.scheduler.model.SchedulerObjectFactory;

 
public class JSCmdProcessClassRemove extends ProcessClassRemove {

	@SuppressWarnings("unused")
	private final String		conClassName	= "JSCmdProcessClassRemove";
	@SuppressWarnings("unused")
	private static final Logger	logger			= Logger.getLogger(JSCmdProcessClassRemove.class);

	public JSCmdProcessClassRemove (SchedulerObjectFactory schedulerObjectFactory) {
		super();
		objFactory = schedulerObjectFactory;
	}
	
    public void setProcessClassIfNotEmpty(String value) {
        if (!isEmpty(value)) {
            super.setProcessClass(value);
        }
    }
}
