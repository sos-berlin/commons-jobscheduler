package com.sos.jobstreams.resolver;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.jobstreams.db.DBItemInConditionCommand;
import com.sos.jobstreams.classes.JobStarter;
import com.sos.jobstreams.classes.JobStarterOptions;
import com.sos.jobstreams.classes.StartJobReturn;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

import sos.xml.SOSXMLXPath;

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


    public StartJobReturn startJob(UUID contextId, SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, JSInCondition inCondition, Map<String, String> listOfParameters) throws NumberFormatException, Exception  {

        StartJobReturn startJobReturn = new StartJobReturn();
        JobStarter jobStarter = new JobStarter();
        JobStarterOptions jobStartOptions = new JobStarterOptions();
        jobStartOptions.setInstanceId(contextId.toString());
        jobStartOptions.setJob(inCondition.getNormalizedJob());
        jobStartOptions.setJobStream(inCondition.getJobStream());
        jobStartOptions.setListOfParameters(listOfParameters);
        jobStartOptions.setNormalizedJob(inCondition.getNormalizedJob());
        String jobXml = jobStarter.buildJobStartXml(jobStartOptions, getCommandParam());
        String answer = "";
        startJobReturn.setStartedJob("");
        if (inCondition.isStartToday()) {
            LOGGER.trace("JSInConditionCommand:startJob XML for job start is: " + jobXml);
            if (schedulerXmlCommandExecutor != null) {
                answer = schedulerXmlCommandExecutor.executeXml(jobXml);

                SOSXMLXPath xPathSchedulerXml = new SOSXMLXPath(new StringBuffer(answer));
                Long taskId = Long.valueOf(xPathSchedulerXml.selectSingleNodeValue("/spooler/answer/ok/task/@id"));
                String errCode = xPathSchedulerXml.selectSingleNodeValue("/spooler/answer/ERROR/@code");
                if (errCode != null) {
                    String errText = xPathSchedulerXml.selectSingleNodeValue("/spooler/answer/ERROR/@text");
                    startJobReturn.setErrCode(errCode);
                    startJobReturn.setErrText(errText);
                    startJobReturn.setStarted(false);
                    LOGGER.warn(errCode + ":" + errText);
                }else {
                    startJobReturn.setStarted(true);
                }
                startJobReturn.setTaskId(taskId);
                LOGGER.trace(answer);
                startJobReturn.setStartedJob(jobStartOptions.getJob());
            } else {
                LOGGER.debug("Start job will be ignored as running in debug  mode.: " + jobStartOptions.getJob());
            }
            executed = true;
        } else {
            LOGGER.debug("Job " + jobStartOptions.getJob() + " will not be started today");
            executed = false;
        }
        LOGGER.trace(answer);
        return startJobReturn;
    }

    public StartJobReturn executeCommand(UUID contextId, SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, JSInCondition inCondition, Map<String, String> listOfParameters) throws NumberFormatException, Exception  {

        StartJobReturn startJobReturn = new StartJobReturn();
        startJobReturn.setStartedJob("");
        String command = getCommand();
        String commandParam = getCommandParam();
        LOGGER.debug("execution command: " + command + " " + commandParam);

        if ("writelog".equalsIgnoreCase(command)) {
            LOGGER.info(commandParam);
            executed = true;
        }
        if ("startjob".equalsIgnoreCase(command)) {
            LOGGER.debug("....starting job:" + inCondition.getJob());
            startJobReturn = startJob(contextId, schedulerXmlCommandExecutor, inCondition, listOfParameters);
        }
        return startJobReturn;
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
