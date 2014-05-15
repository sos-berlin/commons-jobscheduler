package com.sos.scheduler.cmd;

import org.apache.log4j.Level;

public interface ICommandActionsListener {
	public void onMessage(Level level, String message);
}
