package com.sos.eventhandlerservice.resolver;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.sos.eventhandlerservice.db.DBItemInConditionCommand;
import com.sos.exception.SOSException;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSInConditionCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSInConditionCommand.class);
    private DBItemInConditionCommand itemInConditionCommand;

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

        URL url = new URL("http://localhost:4446/joc/api/jobs/start");

        String answer = jobSchedulerRestApiClient.executeRestServiceCommand("post", url, "{\"jobs\":[{\"job\":\"" + job
                + "\",\"at\":\"now\"}],\"jobschedulerId\":\"scheduler_joc_cockpit\",\"auditLog\":{}}");
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
