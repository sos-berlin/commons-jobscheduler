package com.sos.eventhandlerservice.resolver;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.db.DBItemEvent;
import com.sos.eventhandlerservice.db.DBItemInConditionCommand;
import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.jitl.checkrunhistory.JobHistoryHelper;
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
        return itemInConditionCommand.getCommand().replaceAll("_", "").toLowerCase();
    }

    public String getCommandParam() {
        return itemInConditionCommand.getCommandParam();
    }

    private void startJob(JobSchedulerRestApiClient jobSchedulerRestApiClient, String job, String param) throws UnsupportedEncodingException,
            InterruptedException, SOSException, URISyntaxException, MalformedURLException {

        URL url = new URL(settings.getJocUrl() + "/jobs/start");
        String body = "{\"jobs\":[{\"job\":\"" + job + "\",\"at\":\"now\"}],\"jobschedulerId\":\"" + settings.getSchedulerId()
                + "\",\"auditLog\":{}}";

        LOGGER.debug(url.toString());
        LOGGER.debug(body);
        String answer = jobSchedulerRestApiClient.executeRestServiceCommand("post", url, body);
        LOGGER.trace(answer);
    }

    public void executeCommand(JobSchedulerRestApiClient jobSchedulerRestApiClient, JSInCondition jsInCondition) throws UnsupportedEncodingException,
            MalformedURLException, InterruptedException, SOSException, URISyntaxException {
        if ("writelog".equalsIgnoreCase(getCommand())) {
            LOGGER.info(getCommandParam());
        }
        if ("startjob".equalsIgnoreCase(getCommand())) {
            startJob(jobSchedulerRestApiClient, jsInCondition.getJob(), getCommandParam());
        }
        if ("addorder".equalsIgnoreCase(getCommand())) {
            addOrder(jobSchedulerRestApiClient, getCommandParam());
        }
    }

    private void addOrder(JobSchedulerRestApiClient jobSchedulerRestApiClient, String commandParam) throws MalformedURLException, SOSException {
        URL url = new URL(settings.getJocUrl() + "/orders/add");
        JobHistoryHelper jobHistoryHelper = new JobHistoryHelper();
        String orderId = jobHistoryHelper.getOrderId(commandParam);
        String jobChain = jobHistoryHelper.getJobChainName(commandParam);
        
        String body = "{\"jobschedulerId\":\"" + settings.getSchedulerId() + "\",\"orders\":[{\"jobChain\":\"" + jobChain + "\",\"orderId\":\""
                + orderId + "\",\"at\":\"now\"}],\"auditLog\":{}}";
        LOGGER.debug(url.toString());
        LOGGER.debug(body);
        String answer = jobSchedulerRestApiClient.executeRestServiceCommand("post", url, body);
        LOGGER.trace(answer);
    }

}
