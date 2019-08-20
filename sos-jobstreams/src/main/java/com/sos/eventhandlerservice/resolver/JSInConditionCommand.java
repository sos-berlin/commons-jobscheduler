package com.sos.eventhandlerservice.resolver;

import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.classes.JobStartCommand;
import com.sos.eventhandlerservice.db.DBItemInConditionCommand;
import com.sos.exception.SOSException;
import com.sos.jitl.checkrunhistory.JobHistoryHelper;
import com.sos.jitl.restclient.AccessTokenProvider;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;
import com.sos.jitl.restclient.WebserviceCredentials;

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

    private JsonObject jsonFromString(String jsonObjectStr) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }

    private String checkAnswer(String answer) {
        JsonObject executeAnswer = jsonFromString(answer);
        if ((executeAnswer.get("isPermitted") != null && !executeAnswer.getBoolean("isPermitted")) || (executeAnswer.get("isAuthenticated") != null
                && !executeAnswer.getBoolean("isAuthenticated"))) {
            return executeAnswer.get("message").toString();
        } else {
            return "";
        }

    }

    private void startJobs(JobSchedulerRestApiClient jobSchedulerRestApiClient, File privateConf, List<JobStartCommand> listOfJobsToStart)
            throws UnsupportedEncodingException, InterruptedException, SOSException, URISyntaxException, MalformedURLException {

        URL url = new URL(Constants.settings.getJocUrl() + "/jobs/start");
        String jobArray = "";

        for (JobStartCommand jobStartCommand : listOfJobsToStart) {
            jobArray = jobArray + String.format("{\"job\":\"%s\",\"at\":\"%s\"}", jobStartCommand.getJob(), jobStartCommand.getCommandParam()) + ",";
        }
        jobArray = jobArray.substring(0, jobArray.length() - 1);

        String body = "{\"jobs\":[" + jobArray + "],\"jobschedulerId\":\"" + Constants.settings.getSchedulerId() + "\",\"auditLog\":{}}";

        LOGGER.debug(url.toString());
        LOGGER.debug(body);
        String answer = jobSchedulerRestApiClient.executeRestServiceCommand("post", url, body);
        String message = checkAnswer(answer);
        if (!message.isEmpty()) {

            LOGGER.warn(message);

            AccessTokenProvider accessTokenProvider = new AccessTokenProvider(privateConf.getAbsolutePath());
            WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken(null);
            if (webserviceCredentials != null) {
                LOGGER.warn("Trying retry login");
                jobSchedulerRestApiClient.addHeader("X-ACCESS-TOKEN", webserviceCredentials.getAccessToken());
                answer = jobSchedulerRestApiClient.executeRestServiceCommand("post", url, body);
                message = checkAnswer(answer);
                if (!message.isEmpty()) {
                    LOGGER.warn("Retry login failed");
                    LOGGER.warn(message);
                }
            }
        }

        LOGGER.trace(answer);
    }

    public void executeCommand(JobSchedulerRestApiClient jobSchedulerRestApiClient, File privateConf, List<JobStartCommand> listOfJobsToStart)
            throws UnsupportedEncodingException, MalformedURLException, InterruptedException, SOSException, URISyntaxException {
        if ("writelog".equalsIgnoreCase(getCommand())) {
            LOGGER.info(getCommandParam());
        }
        if ("startjobs".equalsIgnoreCase(getCommand())) {
            startJobs(jobSchedulerRestApiClient, privateConf, listOfJobsToStart);
        }
        if ("addorder".equalsIgnoreCase(getCommand())) {
            addOrder(jobSchedulerRestApiClient, privateConf, getCommandParam());
        }
    }

    private void addOrder(JobSchedulerRestApiClient jobSchedulerRestApiClient, File privateConf, String commandParam) throws MalformedURLException,
            SOSException {
        URL url = new URL(Constants.settings.getJocUrl() + "/orders/add");
        JobHistoryHelper jobHistoryHelper = new JobHistoryHelper();
        String orderId = jobHistoryHelper.getOrderId(commandParam);
        String jobChain = jobHistoryHelper.getJobChainName(commandParam);

        String body = "{\"jobschedulerId\":\"" + Constants.settings.getSchedulerId() + "\",\"orders\":[{\"jobChain\":\"" + jobChain
                + "\",\"orderId\":\"" + orderId + "\",\"at\":\"now\"}],\"auditLog\":{}}";
        LOGGER.debug(url.toString());
        LOGGER.debug(body);
        String answer = jobSchedulerRestApiClient.executeRestServiceCommand("post", url, body);
        LOGGER.trace(answer);
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
