package com.sos.jobstreams.resolver;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobstreams.db.DBItemInConditionCommand;
import com.sos.jobstreams.classes.JobStarter;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.job.JobV;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.xml.XMLBuilder;

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

    private void startJob(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, JSInCondition inCondition) throws JocException, JAXBException  {

        JobStarter jobStarter = new JobStarter();
        String jobXml = jobStarter.buildJobStartXml(inCondition, getCommandParam());
        String answer = "";
        String job = inCondition.getNormalizedJob();
        if (inCondition.isStartToday()) {
            LOGGER.trace("JSInConditionCommand:startJob XML for job start ist: " + jobXml);
            if (schedulerXmlCommandExecutor != null) {
                answer = schedulerXmlCommandExecutor.executeXml(jobXml);
            } else {
                LOGGER.debug("Start job will be ignored as running in debug  mode.: " + job);
            }
            executed = true;
        } else {
            LOGGER.debug("Job " + job + " will not be started today");
            executed = false;
        }
        LOGGER.trace(answer);

    }

    public void executeCommand(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, JSInCondition inCondition) throws JocException, JAXBException  {

        String command = getCommand();
        String commandParam = getCommandParam();
        LOGGER.debug("execution command: " + command + " " + commandParam);

        if ("writelog".equalsIgnoreCase(command)) {
            LOGGER.info(commandParam);
            executed = true;
        }
        if ("startjob".equalsIgnoreCase(command)) {
            LOGGER.debug("....starting job:" + inCondition.getJob());
            startJob(schedulerXmlCommandExecutor, inCondition);
        }
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
