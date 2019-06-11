package com.sos.eventhandlerservice.resolver;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.eventhandlerservice.db.DBItemInConditionCommand;
import com.sos.exception.SOSException;
import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;

public class JSInConditionCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSInConditionCommand.class);
    private DBItemInConditionCommand itemInConditionCommand;
    private EventHandlerSettings settings;

    public JSInConditionCommand(EventHandlerSettings settings) {
        super();
        this.settings = settings;
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
        return itemInConditionCommand.getCommand();
    }

    public String getCommandParam() {
        return itemInConditionCommand.getCommandParam();
    }

    private void startJob(JobSchedulerRestApiClient jobSchedulerRestApiClient, String job, String param) throws UnsupportedEncodingException,
            InterruptedException, SOSException, URISyntaxException, MalformedURLException {

        URL url = new URL(settings.getJocUrl() + "/jobs/start");

        String answer = jobSchedulerRestApiClient.executeRestServiceCommand("post", url, "{\"jobs\":[{\"job\":\"" + job
                + "\",\"at\":\"now\"}],\"jobschedulerId\":\"" + settings.getSchedulerId() + "\",\"auditLog\":{}}");
    }

    public void executeCommand(JobSchedulerRestApiClient jobSchedulerRestApiClient, JSInCondition jsInCondition) throws UnsupportedEncodingException,
            MalformedURLException, InterruptedException, SOSException, URISyntaxException {
        if ("showlog".equalsIgnoreCase(getCommand())) {
            LOGGER.info(getCommandParam());
        }
        if ("start_job".equalsIgnoreCase(getCommand())) {
            startJob(jobSchedulerRestApiClient, jsInCondition.getJob(), getCommandParam());
        }
        if ("add_order".equalsIgnoreCase(getCommand())) {
            addOrder(jobSchedulerRestApiClient, getCommandParam());
        }
    }

    private void addOrder(JobSchedulerRestApiClient jobSchedulerRestApiClient, String commandParam) {
        // TODO Auto-generated method stub

    }

}
