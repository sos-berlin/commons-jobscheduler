package com.sos.jobstreams.resolver;

import java.io.File;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobstreams.classes.CheckHistoryCacheRule;
import com.sos.jobstreams.classes.CheckHistoryCondition;
import com.sos.jobstreams.classes.CheckHistoryKey;
import com.sos.jobstreams.classes.CheckHistoryValue;
import com.sos.jobstreams.classes.Constants;
import com.sos.jobstreams.classes.DurationCalculator;
import com.sos.jobstreams.classes.EventDate;
import com.sos.jobstreams.classes.JobStartCommand;
import com.sos.jobstreams.db.DBItemConsumedInCondition;
import com.sos.jobstreams.db.DBItemEvent;
import com.sos.jobstreams.db.DBItemInCondition;
import com.sos.jobstreams.db.DBItemInConditionWithCommand;
import com.sos.jobstreams.db.DBItemOutConditionWithEvent;
import com.sos.jobstreams.db.DBLayerConsumedInConditions;
import com.sos.jobstreams.db.DBLayerEvents;
import com.sos.jobstreams.db.DBLayerInConditionCommands;
import com.sos.jobstreams.db.DBLayerInConditions;
import com.sos.jobstreams.db.DBLayerOutConditionEvents;
import com.sos.jobstreams.db.DBLayerOutConditions;
import com.sos.jobstreams.db.FilterConsumedInConditions;
import com.sos.jobstreams.db.FilterEvents;
import com.sos.jobstreams.db.FilterInConditionCommands;
import com.sos.jobstreams.db.FilterInConditions;
import com.sos.jobstreams.db.FilterOutConditionEvents;
import com.sos.jobstreams.db.FilterOutConditions;
import com.sos.jobstreams.resolver.interfaces.IJSCondition;
import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.classes.event.EventHandlerSettings;
import com.sos.jitl.eventing.evaluate.BooleanExp;
import com.sos.jitl.restclient.AccessTokenProvider;
import com.sos.jitl.restclient.JobSchedulerRestApiClient;
import com.sos.jitl.restclient.WebserviceCredentials;

public class JSConditionResolver {

    private static final String JOB = "job";
    private static final String JOB_CHAIN = "jobchain";
    private static final int JOB_START_BUFFER_SIZE = 30;

    private static final Logger LOGGER = LoggerFactory.getLogger(JSConditionResolver.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private JSJobInConditions jsJobInConditions;
    private JSJobOutConditions jsJobOutConditions;
    private JSEvents jsEvents;
    private SOSHibernateSession sosHibernateSession;
    private BooleanExp booleanExpression;
    private JobSchedulerRestApiClient jobSchedulerRestApiClient;
    private EventHandlerSettings settings;
    private CheckHistoryCondition checkHistoryCondition;
    private List<CheckHistoryCacheRule> listOfCheckHistoryChacheRules;
    private List<JobStartCommand> listOfCommandsToExecute;

    public JSConditionResolver(SOSHibernateSession sosHibernateSession, File privateConf, EventHandlerSettings settings)
            throws UnsupportedEncodingException, InterruptedException, SOSException, URISyntaxException {
        super();
        booleanExpression = new BooleanExp("");
        this.sosHibernateSession = sosHibernateSession;
        this.settings = settings;

        LOGGER.debug("getAccesToken....");
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider(privateConf.getAbsolutePath());
        WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken(null);
        this.settings.setJocUrl(accessTokenProvider.getJocUrl());

        jobSchedulerRestApiClient = new JobSchedulerRestApiClient();
        jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
        jobSchedulerRestApiClient.addHeader("Accept", "application/json");
        if (webserviceCredentials != null) {
            LOGGER.debug("...." + webserviceCredentials.getAccessToken());
            jobSchedulerRestApiClient.addHeader("X-ACCESS-TOKEN", webserviceCredentials.getAccessToken());
        } else {
            LOGGER.warn("Could not create AccessToken");
        }
        checkHistoryCondition = new CheckHistoryCondition(sosHibernateSession, settings.getSchedulerId());
    }

    public JSConditionResolver(SOSHibernateSession sosHibernateSession, String accessToken, String jocUrl) throws UnsupportedEncodingException,
            InterruptedException, SOSException, URISyntaxException {
        super();
        booleanExpression = new BooleanExp("");
        this.sosHibernateSession = sosHibernateSession;
        this.settings = new EventHandlerSettings();
        this.settings.setJocUrl(jocUrl);

        jobSchedulerRestApiClient = new JobSchedulerRestApiClient();
        jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
        jobSchedulerRestApiClient.addHeader("Accept", "application/json");
        jobSchedulerRestApiClient.addHeader("X-ACCESS-TOKEN", accessToken);
        checkHistoryCondition = new CheckHistoryCondition(sosHibernateSession, settings.getSchedulerId());
    }

    private File getPrivateConf() {
        return new File(settings.getConfigDirectory() + "/private/private.conf");
    }

    public void reInit() throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver reinit jobstream model");
        jsJobInConditions = null;
        jsJobOutConditions = null;
        jsEvents = null;
        checkHistoryCondition = new CheckHistoryCondition(sosHibernateSession, settings.getSchedulerId());
        this.init();
    }

    public void init() throws SOSHibernateException {
        // synchronizeJobsWithFileSystem();

        DurationCalculator duration = new DurationCalculator();

        this.listOfCommandsToExecute = new ArrayList<JobStartCommand>();

        if (jsJobInConditions == null) {
            FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
            filterConsumedInConditions.setSession(Constants.getSession());
            DBLayerConsumedInConditions dbLayerCoumsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);
            List<DBItemConsumedInCondition> listOfConsumedInConditions = dbLayerCoumsumedInConditions.getConsumedInConditionsList(
                    filterConsumedInConditions, 0);
            HashMap<Long, DBItemConsumedInCondition> mapOfConsumedInCondition = new HashMap<Long, DBItemConsumedInCondition>();
            for (DBItemConsumedInCondition dbItemConsumedInCondition : listOfConsumedInConditions) {
                mapOfConsumedInCondition.put(dbItemConsumedInCondition.getInConditionId(), dbItemConsumedInCondition);
            }

            FilterInConditions filterInConditions = new FilterInConditions();

            DBLayerInConditions dbLayerInConditions = new DBLayerInConditions(sosHibernateSession);
            List<DBItemInConditionWithCommand> listOfInConditions = dbLayerInConditions.getInConditionsList(filterInConditions, 0);
            for (DBItemInConditionWithCommand dbItemInCondition : listOfInConditions) {
                DBItemInCondition inInCondition = dbItemInCondition.getDbItemInCondition();
                if (inInCondition != null) {
                    dbItemInCondition.setConsumed((mapOfConsumedInCondition.get(dbItemInCondition.getDbItemInCondition().getId()) != null));
                } else {
                    dbItemInCondition.setConsumed(false);
                }
            }
            jsJobInConditions = new JSJobInConditions(settings);
            jsJobInConditions.setListOfJobInConditions(listOfInConditions);

        }

        if (jsJobOutConditions == null) {
            FilterOutConditions filterOutConditions = new FilterOutConditions();
            DBLayerOutConditions dbLayerOutConditions = new DBLayerOutConditions(sosHibernateSession);
            List<DBItemOutConditionWithEvent> listOfOutConditions = dbLayerOutConditions.getOutConditionsList(filterOutConditions, 0);
            jsJobOutConditions = new JSJobOutConditions();
            jsJobOutConditions.setListOfJobOutConditions(listOfOutConditions);
        }

        if (listOfCheckHistoryChacheRules == null) {
            listOfCheckHistoryChacheRules = new ArrayList<CheckHistoryCacheRule>();
            CheckHistoryCacheRule checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("returnCode");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedRunEndedSuccessful");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedRunEndedWithError");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedRunEndedTodaySuccessful");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedRunEndedTodayWithError");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedRunEndedWithError");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedIsEndedBefore");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedSuccessulIsEndedBefore");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedWithErrorIsEndedBefore");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedIsStartedBefore");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedSuccessfulIsStartedBefore");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("lastCompletedWithErrorIsStartedBefore");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isStartedToday");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isStartedTodayCompletedSuccessful");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isStartedTodayCompletedWithError");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isStartedTodayCompleted");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("prev");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("prevSuccessful");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("prevError");
            checkHistoryCacheRule.setValidateAlways(true);
            checkHistoryCacheRule.setValidateIfFalse(false);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isCompletedToday");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isCompletedTodaySuccessfully");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isCompletedTodayWithError");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isCompletedAfter");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isCompletedWithErrorAfter");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isCompletedSuccessfulAfter");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isStartedAfter");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isStartedWithErrorAfter");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

            checkHistoryCacheRule = new CheckHistoryCacheRule();
            checkHistoryCacheRule.setQueryString("isStartedSuccessfulAfter");
            checkHistoryCacheRule.setValidateAlways(false);
            checkHistoryCacheRule.setValidateIfFalse(true);
            listOfCheckHistoryChacheRules.add(checkHistoryCacheRule);

        }

        initEvents();
        initCheckHistory();
        if (isDebugEnabled) {
            duration.end("Init JobStreams condition model ");
            LOGGER.debug("In-Conditions: " + jsJobInConditions.getListOfJobInConditions().size());
            LOGGER.debug("Out-Conditions: " + jsJobOutConditions.getListOfJobOutConditions().size());
            LOGGER.debug("Events: " + jsEvents.getListOfEvents().size());
        }

    }

    private JsonObject jsonFromString(String jsonObjectStr) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }

    private void synchronizeJobsWithFileSystem() {
        LinkedHashSet<String> listOfJobSchedulerJobs = new LinkedHashSet<String>();
        LinkedHashSet<String> listOfJobStreamJobs = new LinkedHashSet<String>();

        DBLayerInConditions dbLayerInConditions = new DBLayerInConditions(sosHibernateSession);
        DBLayerOutConditions dbLayerOutConditions = new DBLayerOutConditions(sosHibernateSession);
        FilterInConditions filterInConditions = new FilterInConditions();
        FilterOutConditions filterOutConditions = new FilterOutConditions();

        try {
            List<DBItemInConditionWithCommand> listOfInconditions = dbLayerInConditions.getInConditionsList(filterInConditions, 0);
            List<DBItemOutConditionWithEvent> listOfOutconditions = dbLayerOutConditions.getOutConditionsList(filterOutConditions, 0);

            listOfInconditions.forEach(item -> {
                listOfJobStreamJobs.add(item.getDbItemInCondition().getJob());
            });

            listOfOutconditions.forEach(item -> {
                listOfJobStreamJobs.add(item.getJob());
            });

            URL url;
            url = new URL(settings.getJocUrl() + "/jobs");
            String body = "{\"jobschedulerId\":\"" + settings.getSchedulerId() + "\",\"compact\":true,\"isOrderJob\":false,\"compactView\":true}";
            LOGGER.debug(url.toString());
            LOGGER.debug(body);
            String answer = jobSchedulerRestApiClient.executeRestServiceCommand("post", url, body);
            JsonObject jsonJobs = jsonFromString(answer);
            JsonArray jobs = jsonJobs.getJsonArray("jobs");
            if (jobs != null && jobs.size() > 0) {
                for (int i = 0; i < jobs.size(); i++) {
                    JsonObject job = jobs.getJsonObject(i);
                    if (job != null) {
                        String jobName = job.getString("path");
                        listOfJobSchedulerJobs.add(jobName);
                    }
                }
            }

            listOfJobStreamJobs.forEach(jobName -> {
                if (!listOfJobSchedulerJobs.contains(jobName)) {
                    removeJob(jobName);
                }
            });

        } catch (MalformedURLException e) {
            LOGGER.warn("Could not synchronize database with filesystem: " + e.getMessage());
        } catch (SOSException e) {
            LOGGER.warn("Could not synchronize database with filesystem: " + e.getMessage());
        }

    }

    public void initEvents() throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::initEvents");
        if (jsEvents == null) {
            jsEvents = new JSEvents();
            FilterEvents filterEvents = new FilterEvents();
            // filterEvents.setSession(Constants.getSession());
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            List<DBItemEvent> listOfEvents = dbLayerEvents.getEventsList(filterEvents, 0);
            jsEvents.setListOfEvents(listOfEvents);
        }
    }

    public void initCheckHistory() throws SOSHibernateException {

        LOGGER.debug("JSConditionResolve::initCheckHistory");
        for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
            for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                String expressionValue = inCondition.getExpression();
                JSConditions jsConditions = new JSConditions();
                List<JSCondition> listOfConditions = jsConditions.getListOfConditions(expressionValue);
                for (JSCondition jsCondition : listOfConditions) {
                    try {
                        switch (jsCondition.getConditionType()) {
                        case JOB: {
                            checkHistoryCondition.validateJob(jsCondition, inCondition.getJob(), 0);
                            break;
                        }
                        case JOB_CHAIN: {
                            checkHistoryCondition.validateJobChain(jsCondition);
                            break;
                        }
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Could not validate expression:" + expressionValue + " in in-condition of job " + inCondition.getJob());
                    }
                }
            }
        }

        for (JSOutConditions jobOutConditions : jsJobOutConditions.getListOfJobOutConditions().values()) {
            for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                String expressionValue = outCondition.getExpression() + " ";
                JSConditions jsConditions = new JSConditions();
                List<JSCondition> listOfConditions = jsConditions.getListOfConditions(expressionValue);
                for (JSCondition jsCondition : listOfConditions) {
                    try {
                        switch (jsCondition.getConditionType()) {
                        case JOB: {
                            checkHistoryCondition.validateJob(jsCondition, outCondition.getJob(), 0);
                            break;
                        }
                        case JOB_CHAIN: {
                            checkHistoryCondition.validateJobChain(jsCondition);
                            break;
                        }
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Could not validate expression:" + expressionValue + " in in-condition of job " + outCondition.getJob());
                    }
                }
            }
        }
    }

    public boolean validate(Integer taskReturnCode, IJSCondition condition) {
        String expressionValue = condition.getExpression() + " ";
        JSConditions jsConditions = new JSConditions();
        List<JSCondition> listOfConditions = jsConditions.getListOfConditions(expressionValue);
        LOGGER.trace("JSConditionResolve::validate");
        for (JSCondition jsCondition : listOfConditions) {
            LOGGER.trace("JSConditionResolve::validate --> " + jsCondition.getConditionType());

            switch (jsCondition.getConditionType()) {
            case "returncode": {
                JSReturnCodeResolver returnCodeResolver = new JSReturnCodeResolver();
                if (returnCodeResolver.resolve(taskReturnCode, jsCondition.getConditionParam())) {
                    expressionValue = expressionValue.replace(jsCondition.getConditionType() + ":" + jsCondition.getConditionParam() + " ", "true ");
                }

                break;
            }
            case "fileexist": {
                File f = new File(jsCondition.getConditionParam());
                if (f.exists()) {
                    expressionValue = expressionValue.replace(jsCondition.getConditionType() + ":" + jsCondition.getConditionParam() + " ", "true ");
                }

                break;
            }
            case JOB: {
                try {
                    if (checkHistoryCondition.validateJob(jsCondition, condition.getJob(), taskReturnCode).getValidateResult()) {
                        expressionValue = expressionValue.replace(jsCondition.getConditionType() + ":" + jsCondition.getConditionParam() + " ",
                                "true ");
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                break;
            }
            case JOB_CHAIN: {
                try {
                    if (checkHistoryCondition.validateJobChain(jsCondition).getValidateResult()) {
                        expressionValue = expressionValue.replace(jsCondition.getConditionType() + ":" + jsCondition.getConditionParam() + " ",
                                "true ");
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                break;
            }
            case "event": {
                String event = jsCondition.getEventName();

                JSEventKey jsEventKey = new JSEventKey();
                jsEventKey.setEvent(event);

                EventDate eventDate = new EventDate();
                String date = jsCondition.getConditionDate();
                try {
                    if (eventDate.isPrev(date)) {
                        CheckHistoryValue checkHistoryValue = checkHistoryCondition.getPrev(date, condition.getJob());
                        date = String.valueOf(checkHistoryValue.getStartTime().getMonthValue()) + "." + String.valueOf(checkHistoryValue
                                .getStartTime().getDayOfMonth());
                    }
                } catch (Exception e) {
                    LOGGER.warn("Could not calculate prev date for: " + jsCondition.getConditionJob());
                }
                jsEventKey.setSession(eventDate.getEventDate(date));
                JSEvent jsEvent = jsEvents.getEventByJobStream(jsEventKey, jsCondition.getConditionJobStream());
                if (jsEvent != null) {
                    expressionValue = expressionValue.replace(jsCondition.getConditonValue() + " ", "true ");
                }

                break;
            }
            default:
                LOGGER.warn("unknown conditionType: " + jsCondition.getConditionType());
            }

        }
        LOGGER.trace("ExpressionValue: " + expressionValue);
        booleanExpression.setBoolExp(expressionValue);
        boolean evaluatedExpression = booleanExpression.evaluateExpression();
        LOGGER.trace("Evaluated expressionValue: " + evaluatedExpression);
        return evaluatedExpression;
    }

    public List<JSInCondition> resolveInConditions() throws UnsupportedEncodingException, MalformedURLException, InterruptedException, SOSException,
            URISyntaxException {
        List<JSInCondition> listOfValidatedInconditions = new ArrayList<JSInCondition>();
        LOGGER.debug("JSConditionResolve::resolveInConditions");
        for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
            for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                if (!inCondition.isConsumed()) {
                    String expression = inCondition.getJob() + ":" + inCondition.getExpression();

                    LOGGER.trace("---InCondition: " + expression);
                    if (validate(null, inCondition)) {
                        this.executeCommands(inCondition);
                        listOfValidatedInconditions.add(inCondition);
                    } else {
                        LOGGER.trace(expression + "-->false");
                    }
                } else {
                    LOGGER.trace(inCondition.getExpression() + "-->already consumed");
                }
                LOGGER.trace("");
            }
        }
        this.flushCommands(jobSchedulerRestApiClient);
        return listOfValidatedInconditions;
    }

    public void executeCommands(JSInCondition inCondition) throws UnsupportedEncodingException, MalformedURLException, InterruptedException,
            SOSException, URISyntaxException {

        LOGGER.trace("execute commands ------>");
        if (inCondition.isMarkExpression()) {
            inCondition.markAsConsumed(sosHibernateSession);
        }

        for (JSInConditionCommand inConditionCommand : inCondition.getListOfInConditionCommand()) {
            if ("startjob".equals(inConditionCommand.getCommand())) {
                JobStartCommand jobStartCommand = new JobStartCommand();
                jobStartCommand.setJob(inCondition.getJob());
                jobStartCommand.setCommandParam(inConditionCommand.getCommandParam());
                listOfCommandsToExecute.add(jobStartCommand);
                if (listOfCommandsToExecute.size() > JOB_START_BUFFER_SIZE) {
                    this.flushCommands(jobSchedulerRestApiClient);
                }
            } else {
                inConditionCommand.executeCommand(jobSchedulerRestApiClient, getPrivateConf(), null);
            }
        }
    }

    public void flushCommands(JobSchedulerRestApiClient jobSchedulerRestApiClient) throws UnsupportedEncodingException, MalformedURLException,
            InterruptedException, SOSException, URISyntaxException {
        LOGGER.debug("JSConditionResolve::flushCommands");
        if (this.listOfCommandsToExecute.size() > 0) {
            JSInConditionCommand inConditionCommand = new JSInConditionCommand();
            inConditionCommand.setCommand("startjobs");
            inConditionCommand.setCommandParam("now");

            inConditionCommand.executeCommand(jobSchedulerRestApiClient, getPrivateConf(), listOfCommandsToExecute);
            this.listOfCommandsToExecute = new ArrayList<JobStartCommand>();
        }

    }

    public JSEvents resolveOutConditions(Integer taskReturnCode, String jobSchedulerId, String job) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::resolveOutConditions");
        JSJobConditionKey jobConditionKey = new JSJobConditionKey();
        JSEvents newJsEvents = new JSEvents();
        jobConditionKey.setJob(job);
        jobConditionKey.setJobSchedulerId(jobSchedulerId);
        JSOutConditions jobOutConditions = jsJobOutConditions.getListOfJobOutConditions().get(jobConditionKey);

        if (jobOutConditions != null) {
            for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                String expression = outCondition.getJob() + ":" + outCondition.getExpression();

                LOGGER.trace("---OutCondition: " + expression);
                if (validate(taskReturnCode, outCondition)) {
                    LOGGER.trace("create events ------>");
                    outCondition.storeOutConditionEvents(sosHibernateSession, newJsEvents);

                } else {
                    LOGGER.trace(expression + "-->false");
                }
                LOGGER.trace("");
            }
        }
        jsEvents.addAll(newJsEvents.getListOfEvents());
        return newJsEvents;
    }

    public void resolveOutConditions() {
        for (JSOutConditions jobOutConditions : jsJobOutConditions.getListOfJobOutConditions().values()) {
            for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                String expression = outCondition.getJob() + ":" + outCondition.getExpression();

                LOGGER.trace("---OutCondition: " + expression);
                if (validate(null, outCondition)) {
                    LOGGER.trace(expression + "-->true");
                } else {
                    LOGGER.trace(expression + "-->false");
                }
                LOGGER.trace("");
            }
        }
    }

    public BooleanExp getBooleanExpression() {
        return booleanExpression;
    }

    public void removeConsumedInconditions(FilterConsumedInConditions filterConsumedInConditions) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::removeConsumedInconditions --> " + filterConsumedInConditions.getJobStream() + "."
                + filterConsumedInConditions.getJob());

        try {
            DBLayerConsumedInConditions dbLayerConsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerConsumedInConditions.deleteConsumedInConditions(filterConsumedInConditions);
            sosHibernateSession.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            sosHibernateSession.rollback();
        }

        for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
            for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                String expression = inCondition.getJob() + ":" + inCondition.getExpression();
                if ((filterConsumedInConditions.getJobStream().equals(inCondition.getJobStream()) || filterConsumedInConditions.getJobStream()
                        .isEmpty()) && (filterConsumedInConditions.getJob().equals(inCondition.getJob()) || filterConsumedInConditions.getJob()
                                .isEmpty()) && inCondition.isConsumed()) {
                    LOGGER.trace(expression + " no longer consumed");
                    inCondition.setConsumed(false);
                }
            }
        }
    }

    public void removeEventsFromJobStream(FilterEvents filter) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::removeConsumedInconditions --> " + filter.getJobStream() + "." + filter.getJob());
        try {
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.deleteEventsFromJobStream(filter);
            sosHibernateSession.commit();

            jsEvents = null;
            initEvents();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            sosHibernateSession.rollback();
        }
    }

    public void addEvent(FilterEvents filterEvents) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::addEvent --> " + filterEvents.getJobStream() + "." + filterEvents.getEvent());
        JSEvent event = new JSEvent();
        DBItemEvent itemEvent = new DBItemEvent();
        itemEvent.setCreated(new Date());
        itemEvent.setEvent(filterEvents.getEvent());
        itemEvent.setOutConditionId(filterEvents.getOutConditionId());
        itemEvent.setSession(filterEvents.getSession());
        itemEvent.setJobStream(filterEvents.getJobStream());
        event.setItemEvent(itemEvent);
        jsEvents.addEvent(event);
        LOGGER.debug(filterEvents.getEvent() + " added");

        try {
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.setAutoCommit(false);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.store(itemEvent);
            sosHibernateSession.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            sosHibernateSession.rollback();
        }
    }

    public void removeEvent(FilterEvents filterEvents) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::removeEvent --> " + filterEvents.getJobStream() + "." + filterEvents.getEvent());
        DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);

        sosHibernateSession.setAutoCommit(false);

        try {
            sosHibernateSession.beginTransaction();
            dbLayerEvents.delete(filterEvents);
            sosHibernateSession.commit();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            sosHibernateSession.rollback();
        }

        jsEvents = null;
        initEvents();
        LOGGER.debug(filterEvents.getEvent() + " removed");

    }

    public Boolean eventExist(JSEventKey jsEventKey, String jobStream) {
        JSEvent jsEvent = jsEvents.getEventByJobStream(jsEventKey, jobStream);
        return jsEvent != null;
    }

    public void checkHistoryCache(String jobPath, Integer taskReturnCode) throws Exception {
        CheckHistoryKey checkHistoryKey = new CheckHistoryKey(JOB, jobPath, "");
        for (CheckHistoryCacheRule checkHistoryCacheRule : listOfCheckHistoryChacheRules) {
            checkHistoryKey.setQuery(checkHistoryCacheRule.getQueryString());
            CheckHistoryValue validateResult = checkHistoryCondition.getCache(checkHistoryKey);
            if (validateResult != null && ((checkHistoryCacheRule.isValidateAlways()) || (checkHistoryCacheRule.isValidateIfFalse() && !validateResult
                    .getValidateResult()))) {
                checkHistoryCondition.putCache(checkHistoryKey, null);
                checkHistoryCondition.validateJob(validateResult.getJsCondition(), jobPath, taskReturnCode);
            }
        }
    }

    public void removeJob(String job) {
        FilterInConditions filterInConditions = new FilterInConditions();
        FilterInConditionCommands filterInConditionCommands = new FilterInConditionCommands();
        FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();

        filterConsumedInConditions.setJob(job);
        filterInConditions.setJob(job);
        filterInConditionCommands.setJob(job);

        DBLayerInConditions dbLayerInConditions = new DBLayerInConditions(sosHibernateSession);
        DBLayerInConditionCommands dbLayerInConditionCommands = new DBLayerInConditionCommands(sosHibernateSession);
        DBLayerConsumedInConditions dbLayerConsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);
        try {
            sosHibernateSession.beginTransaction();
            // try {
            // dbLayerInConditionCommands.deleteCommandWithInConditions(filterInConditionCommands);
            // dbLayerConsumedInConditions.deleteConsumedInConditions(filterConsumedInConditions);
            // dbLayerInConditions.delete(filterInConditions);
            // } catch (SOSHibernateException e1) {
            // LOGGER.warn("Could not delete jobs from EventHandler In-Conditions after
            // deleting jobs from filesystem: " + e1.getMessage());
            // }

            DBLayerOutConditions dbLayerOutConditions = new DBLayerOutConditions(sosHibernateSession);
            DBLayerOutConditionEvents dbLayerOutConditionEvents = new DBLayerOutConditionEvents(sosHibernateSession);
            DBLayerEvents dbEvents = new DBLayerEvents(sosHibernateSession);
            FilterOutConditions filterOutConditions = new FilterOutConditions();
            FilterOutConditionEvents filterOutConditionEvents = new FilterOutConditionEvents();
            FilterEvents filterEvents = new FilterEvents();
            filterOutConditions.setJob(job);
            filterOutConditionEvents.setJob(job);
            filterEvents.setJob(job);

            // try {
            // dbLayerOutConditionEvents.deleteEventsWithOutConditions(filterOutConditionEvents);
            // dbEvents.deleteEventsWithOutConditions(filterEvents);
            // dbLayerOutConditions.delete(filterOutConditions);
            // } catch (SOSHibernateException e1) {
            // LOGGER.warn("Could not delete jobs from EventHandler Out-Conditions after
            // deleting jobs from filesystem: " + e1.getMessage());
            // }
            sosHibernateSession.commit();

        } catch (SOSHibernateException e2) {
            LOGGER.warn("Could not create transaction for deleting jobs from EventHandle after deleting jobs from filesystem: " + e2.getMessage());
        } finally {
            try {
                sosHibernateSession.rollback();
            } catch (SOSHibernateException e) {
                LOGGER.warn("Could not rollback transaction for deleting jobs from EventHandle after deleting jobs from filesystem: " + e
                        .getMessage());
            }
        }

    }

    public void setReportingSession(SOSHibernateSession reportingSession) {
        this.sosHibernateSession = reportingSession;
    }
}
