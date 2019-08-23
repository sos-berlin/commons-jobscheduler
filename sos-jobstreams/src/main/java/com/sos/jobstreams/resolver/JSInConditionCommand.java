package com.sos.jobstreams.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobstreams.db.DBItemInConditionCommand;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;
import com.sos.xml.XMLBuilder;

public class JSInConditionCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSInConditionCommand.class);
    private DBItemInConditionCommand itemInConditionCommand;

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
        return itemInConditionCommand.getCommand().replaceAll("_", "").toLowerCase();
    }

    public String getCommandParam() {
        return itemInConditionCommand.getCommandParam();
    }

    private String buildJobStartXml(JSInCondition inCondition) {
        XMLBuilder xml = new XMLBuilder("start_job");

        xml.addAttribute("job", inCondition.getNormalizedJob()).addAttribute("force", "yes");
        if (getCommandParam() == null || getCommandParam().isEmpty()) {
            xml.addAttribute("at", "now");
        } else {
            xml.addAttribute("at", getCommandParam());

        }
        return xml.asXML();
    }

    private void startJob(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, JSInCondition inCondition) {

        String jobXml = buildJobStartXml(inCondition);
        String answer = "";
        LOGGER.trace("JSInConditionCommand:startJob XML for job start ist: " + jobXml);
        if (schedulerXmlCommandExecutor != null) {
            answer = schedulerXmlCommandExecutor.executeXml(jobXml);
        } else {
            LOGGER.debug("Start job: " + inCondition.getNormalizedJob());
        }
        LOGGER.trace(answer);

    }

    public void executeCommand(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, JSInCondition inCondition) {

        String command = getCommand();
        String commandParam = getCommandParam();
        LOGGER.debug("execution command: " + command + " " + commandParam);

        if ("writelog".equalsIgnoreCase(command)) {
            LOGGER.info(commandParam);
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

}
