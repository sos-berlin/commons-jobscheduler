package com.sos.jobstreams.resolver;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobstreams.db.DBItemInConditionCommand;
import com.sos.jobstreams.classes.JobStarter;
import com.sos.jobstreams.classes.JobStarterOptions;
import com.sos.joc.exceptions.JocException;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class JSInConditionCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSInConditionCommand.class);
    private DBItemInConditionCommand itemInConditionCommand;
    private boolean executed;

    public JSInConditionCommand() {
        super();
    }

    public void setItemInConditionCommand(DBItemInConditionCommand itemInConditionCommand) {
        this.itemInConditionCommand = itemInConditionCommand;
    }

    public Long getId() {
        return itemInConditionCommand.getId();
    }

    public Long getInConditionId() {
        return itemInConditionCommand.getInConditionId();
    }

    public String getCommand() {
        if (itemInConditionCommand.getCommand() != null) {
            return itemInConditionCommand.getCommand().replaceAll("_", "").toLowerCase();
        } else {
            return "***";
        }
    }

    public String getCommandParam() {
        return itemInConditionCommand.getCommandParam();
    }

    private String startJob(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, JSInCondition inCondition) throws JocException, JAXBException  {

    	 JobStarter jobStarter = new JobStarter();
         JobStarterOptions jobStartOptions = new JobStarterOptions();
         jobStartOptions.setJob(inCondition.getNormalizedJob());
         jobStartOptions.setJobStream(inCondition.getJobStream());
         jobStartOptions.setNormalizedJob(inCondition.getNormalizedJob());
         String jobXml = jobStarter.buildJobStartXml(jobStartOptions, getCommandParam());

        String answer = "";
        String job = inCondition.getNormalizedJob();
        String startedJob = "";
        if (inCondition.isStartToday()) {
            LOGGER.trace("JSInConditionCommand:startJob XML for job start ist: " + jobXml);
            if (schedulerXmlCommandExecutor != null) {
                answer = schedulerXmlCommandExecutor.executeXml(jobXml);
                LOGGER.trace(answer);
                startedJob = job;
            } else {
                LOGGER.debug("Start job will be ignored as running in debug  mode.: " + job);
            }
            executed = true;
        } else {
            LOGGER.debug("Job " + job + " will not be started today");
            executed = false;
        }
        LOGGER.trace(answer);
        return startedJob;
    }

    public String executeCommand(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, JSInCondition inCondition) throws JocException, JAXBException  {

        String startedJob = "";
        String command = getCommand();
        String commandParam = getCommandParam();
        LOGGER.debug("execution command: " + command + " " + commandParam);

        if ("writelog".equalsIgnoreCase(command)) {
            LOGGER.info(commandParam);
            executed = true;
        }
        if ("startjob".equalsIgnoreCase(command)) {
            LOGGER.debug("....starting job:" + inCondition.getJob());
            startedJob = startJob(schedulerXmlCommandExecutor, inCondition);
        }
        return startedJob;
    }

    public void setCommand(String command) {
        if (this.itemInConditionCommand == null) {
            this.itemInConditionCommand = new DBItemInConditionCommand();
        }
        this.itemInConditionCommand.setCommand(command);
    }

    public void setCommandParam(String commandParam) {
        if (this.itemInConditionCommand == null) {
            this.itemInConditionCommand = new DBItemInConditionCommand();
        }
        this.itemInConditionCommand.setCommandParam(commandParam);

    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }
}
