package com.sos.jobstreams.resolver;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobstreams.db.DBItemInConditionCommand;
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

    private Map<String, String> getMapOfAttributes(String commandParam) {

        Map<String, String> listOfAttributes = new HashMap<String, String>();
        if (commandParam == null || commandParam.isEmpty()) {
            listOfAttributes.put("at", "now");
        } else {
            String name = "";
            String value = "";
            String[] attributes = commandParam.split(",");
            for (int i = 0; i < attributes.length; i++) {
                String attribute = attributes[i];
                String[] parts = attribute.split("=");
                if (parts.length == 1) {
                    value = parts[0];
                    name = "at";
                } else if (parts.length == 2) {
                    value = parts[1];
                    name = parts[0];
                }
                listOfAttributes.put(name, value);
            }
        }
        return listOfAttributes;
    }

    public Map<String, String> testGetMapOfAttributes(String commandParam) {
        return getMapOfAttributes(commandParam);
    }

    private String buildJobStartXml(JSInCondition inCondition) {
        XMLBuilder xml = new XMLBuilder("start_job");

        xml.addAttribute("job", inCondition.getNormalizedJob()).addAttribute("force", "no");
        Map<String, String> listOfAttributes = getMapOfAttributes(getCommandParam());
        listOfAttributes.forEach((name, value) -> xml.addAttribute(name, value));
        return xml.asXML();
    }

    private void startJob(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, JSInCondition inCondition) {

        String jobXml = buildJobStartXml(inCondition);
        String answer = "";
        String job = inCondition.getNormalizedJob();
        if (inCondition.isStartToday()) {
            LOGGER.trace("JSInConditionCommand:startJob XML for job start ist: " + jobXml);
            if (schedulerXmlCommandExecutor != null) {
                answer = schedulerXmlCommandExecutor.executeXml(jobXml);
            } else {
                LOGGER.debug("Start job: " + job);
            }
            executed = true;
        } else {
            LOGGER.debug("Job " + job + " will not be started today");
            executed = false;
        }
        LOGGER.trace(answer);

    }

    public void executeCommand(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, JSInCondition inCondition) {

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
