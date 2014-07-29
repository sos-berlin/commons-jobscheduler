package com.sos.JSHelper.interfaces;

public interface IJobSchedulerLoggingAppender {

	/**
	 * @see org.apache.log4j.FileAppender#activateOptions() */
	public abstract void activateOptions();

	public abstract boolean hasLogger();
}