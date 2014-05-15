package com.sos.scheduler.cmd;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.commands.JSCmdKillTask;
import org.apache.log4j.Logger;

import java.math.BigInteger;

public class JSKillTask extends JSCommand {

	private final Logger logger = Logger.getLogger(JSKillTask.class);

	private final JSCmdKillTask cmdKillTask;

	public JSKillTask(String host, Integer port, Integer taskId, String jobName, Boolean immediately) {
        super(host, port);
        this.cmdKillTask = setCommand(taskId, jobName, immediately);
	}

	public JSKillTask(SchedulerObjectFactory factory, Integer taskId, String jobName, Boolean immediately) {
        super(factory);
		this.cmdKillTask = setCommand(taskId, jobName, immediately);
	}

    private JSCmdKillTask setCommand(Integer taskId, String jobName, Boolean immediately) {
        JSCmdKillTask cmd = getFactory().createKillTask();
        cmd.setId(BigInteger.valueOf(taskId));
        cmd.setJob(jobName);
        cmd.setImmediately((immediately)?"yes":"no");
        setCommand(cmd);
        logger.info("Try to kill task " + jobName +" with taskID " + taskId  + ((immediately) ? " immediately" : "") );
        return cmd;
    }

}
