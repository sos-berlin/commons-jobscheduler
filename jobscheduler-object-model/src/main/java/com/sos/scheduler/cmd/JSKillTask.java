package com.sos.scheduler.cmd;

import com.sos.scheduler.model.SchedulerObjectFactory;
import com.sos.scheduler.model.commands.JSCmdKillTask;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSKillTask extends JSCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSKillTask.class);

    @SuppressWarnings("unused")
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
        cmd.setImmediately((immediately) ? "yes" : "no");
        setCommand(cmd);
        LOGGER.info("Try to kill task " + jobName + " with taskID " + taskId + ((immediately) ? " immediately" : ""));
        return cmd;
    }

}
