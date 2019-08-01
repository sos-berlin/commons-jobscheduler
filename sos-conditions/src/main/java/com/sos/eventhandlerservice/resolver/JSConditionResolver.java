package com.sos.eventhandlerservice.resolver;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.eventhandlerservice.classes.CheckHistoryCacheRule;
import com.sos.eventhandlerservice.classes.CheckHistoryCondition;
import com.sos.eventhandlerservice.classes.CheckHistoryKey;
import com.sos.eventhandlerservice.classes.CheckHistoryValue;
import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.classes.EventDate;
import com.sos.eventhandlerservice.db.DBItemConsumedInCondition;
import com.sos.eventhandlerservice.db.DBItemEvent;
import com.sos.eventhandlerservice.db.DBItemInConditionWithCommand;
import com.sos.eventhandlerservice.db.DBItemOutConditionWithEvent;
import com.sos.eventhandlerservice.db.DBLayerConsumedInConditions;
import com.sos.eventhandlerservice.db.DBLayerEvents;
import com.sos.eventhandlerservice.db.DBLayerInConditionCommands;
import com.sos.eventhandlerservice.db.DBLayerInConditions;
import com.sos.eventhandlerservice.db.DBLayerOutConditions;
import com.sos.eventhandlerservice.db.FilterConsumedInConditions;
import com.sos.eventhandlerservice.db.FilterEvents;
import com.sos.eventhandlerservice.db.FilterInConditionCommands;
import com.sos.eventhandlerservice.db.FilterInConditions;
import com.sos.eventhandlerservice.db.FilterOutConditions;
import com.sos.eventhandlerservice.resolver.interfaces.IJSCondition;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(JSConditionResolver.class);

    private JSJobInConditions jsJobInConditions;
    private JSJobOutConditions jsJobOutConditions;
    private JSEvents jsEvents;
    private SOSHibernateSession sosHibernateSession;
    private BooleanExp booleanExpression;
    private JobSchedulerRestApiClient jobSchedulerRestApiClient;
    private EventHandlerSettings settings;
    private CheckHistoryCondition checkHistoryCondition;
    private List<CheckHistoryCacheRule> listOfCheckHistoryChacheRules;

    public JSConditionResolver(SOSHibernateSession sosHibernateSession, EventHandlerSettings settings) throws UnsupportedEncodingException,
            InterruptedException, SOSException, URISyntaxException {
        super();
        booleanExpression = new BooleanExp("");
        this.sosHibernateSession = sosHibernateSession;
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider();
        WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken(null);
        this.settings.setJocUrl(accessTokenProvider.getJocUrl());

        jobSchedulerRestApiClient = new JobSchedulerRestApiClient();
        jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
        jobSchedulerRestApiClient.addHeader("Accept", "application/json");
        jobSchedulerRestApiClient.addHeader("X-ACCESS-TOKEN", webserviceCredentials.getAccessToken());
        checkHistoryCondition = new CheckHistoryCondition(sosHibernateSession, settings.getSchedulerId());
    }

    public JSConditionResolver(SOSHibernateSession sosHibernateSession, File privateConf, EventHandlerSettings settings)
            throws UnsupportedEncodingException, InterruptedException, SOSException, URISyntaxException {
        super();
        booleanExpression = new BooleanExp("");
        this.sosHibernateSession = sosHibernateSession;
        this.settings = settings;

        AccessTokenProvider accessTokenProvider = new AccessTokenProvider(privateConf.getAbsolutePath());
        WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken(null);
        this.settings.setJocUrl(accessTokenProvider.getJocUrl());

        jobSchedulerRestApiClient = new JobSchedulerRestApiClient();
        jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
        jobSchedulerRestApiClient.addHeader("Accept", "application/json");
        jobSchedulerRestApiClient.addHeader("X-ACCESS-TOKEN", webserviceCredentials.getAccessToken());
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

    public void reInit() throws SOSHibernateException {
        jsJobInConditions = null;
        jsJobOutConditions = null;
        jsEvents = null;
        checkHistoryCondition = new CheckHistoryCondition(sosHibernateSession, settings.getSchedulerId());
        this.init();
    }

    public void init() throws SOSHibernateException {

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
                dbItemInCondition.setConsumed((mapOfConsumedInCondition.get(dbItemInCondition.getDbItemInCondition().getId()) != null));
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

    }

    public void initEvents() throws SOSHibernateException {
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
                String expressionValue = outCondition.getExpression();
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
        String expressionValue = condition.getExpression();
        JSConditions jsConditions = new JSConditions();
        List<JSCondition> listOfConditions = jsConditions.getListOfConditions(expressionValue);
        for (JSCondition jsCondition : listOfConditions) {
            switch (jsCondition.getConditionType()) {
            case "returncode": {
                JSReturnCodeResolver returnCodeResolver = new JSReturnCodeResolver();
                if (returnCodeResolver.resolve(taskReturnCode, jsCondition.getConditionParam())) {
                    expressionValue = expressionValue.replace(jsCondition.getConditionType() + ":" + jsCondition.getConditionParam(), "true");
                }

                break;
            }
            case "fileexist": {
                File f = new File(jsCondition.getConditionParam());
                if (f.exists()) {
                    expressionValue = expressionValue.replace(jsCondition.getConditionType() + ":" + jsCondition.getConditionParam(), "true");
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
        booleanExpression.setBoolExp(expressionValue);
        return booleanExpression.evaluateExpression();
    }

    public List<JSInCondition> resolveInConditions() throws UnsupportedEncodingException, MalformedURLException, InterruptedException, SOSException,
            URISyntaxException {
        List<JSInCondition> listOfValidatedInconditions = new ArrayList<JSInCondition>();
        for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
            for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                String expression = inCondition.getJob() + ":" + inCondition.getExpression();

                LOGGER.debug("---InCondition: " + expression);
                if (validate(null, inCondition)) {
                    if (!inCondition.isConsumed()) {
                        inCondition.executeCommands(sosHibernateSession, jobSchedulerRestApiClient);
                        listOfValidatedInconditions.add(inCondition);
                    } else {
                        LOGGER.debug(inCondition.getExpression() + "-->already consumed");
                    }
                } else {
                    LOGGER.debug(expression + "-->false");
                }
                LOGGER.debug("");
            }
        }
        return listOfValidatedInconditions;
    }

    public JSEvents resolveOutConditions(Integer taskReturnCode, String jobSchedulerId, String job) throws SOSHibernateException {
        JSJobConditionKey jobConditionKey = new JSJobConditionKey();
        JSEvents newJsEvents = new JSEvents();
        jobConditionKey.setJob(job);
        jobConditionKey.setJobSchedulerId(jobSchedulerId);
        JSOutConditions jobOutConditions = jsJobOutConditions.getListOfJobOutConditions().get(jobConditionKey);

        if (jobOutConditions != null) {
            for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                String expression = outCondition.getJob() + ":" + outCondition.getExpression();

                LOGGER.debug("---OutCondition: " + expression);
                if (validate(taskReturnCode, outCondition)) {
                    LOGGER.debug("create events ------>");
                    outCondition.storeOutConditionEvents(sosHibernateSession, newJsEvents);

                } else {
                    LOGGER.debug(expression + "-->false");
                }
                LOGGER.debug("");
            }
        }
        jsEvents.addAll(newJsEvents.getListOfEvents());
        return newJsEvents;
    }

    public void resolveOutConditions() {
        for (JSOutConditions jobOutConditions : jsJobOutConditions.getListOfJobOutConditions().values()) {
            for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                String expression = outCondition.getJob() + ":" + outCondition.getExpression();

                LOGGER.debug("---OutCondition: " + expression);
                if (validate(null, outCondition)) {
                    LOGGER.debug(expression + "-->true");
                } else {
                    LOGGER.debug(expression + "-->false");
                }
                LOGGER.debug("");
            }
        }
    }

    public BooleanExp getBooleanExpression() {
        return booleanExpression;
    }

    public void removeConsumedInconditions(FilterConsumedInConditions filterConsumedInConditions) throws SOSHibernateException {

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
                if (filterConsumedInConditions.getJobStream().equals(inCondition.getJobStream()) && (filterConsumedInConditions.getJob().equals(
                        inCondition.getJob()) || filterConsumedInConditions.getJob().isEmpty()) && inCondition.isConsumed()) {
                    LOGGER.debug(expression + " no longer consumed");
                    inCondition.setConsumed(false);
                }
            }
        }
    }

    public void removeEventsFromJobStream(FilterEvents filter) throws SOSHibernateException {
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
        JSEvent event = new JSEvent();
        DBItemEvent itemEvent = new DBItemEvent();
        itemEvent.setCreated(new Date());
        itemEvent.setEvent(filterEvents.getEvent());
        itemEvent.setOutConditionId(filterEvents.getOutConditionId());
        itemEvent.setSession(filterEvents.getSession());
        itemEvent.setJobStream(filterEvents.getJobStream());
        event.setItemEvent(itemEvent);
        jsEvents.addEvent(event);

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
            dbLayerInConditionCommands.deleteCommandWithInConditions(filterInConditionCommands);
            dbLayerConsumedInConditions.deleteConsumedInConditions(filterConsumedInConditions);
            dbLayerInConditions.delete(filterInConditions);
        } catch (SOSHibernateException e1) {
            LOGGER.warn("Could not delete jobs from EventHandler after deleting jobs from filesystem: " + e1.getMessage());
        }

        try {
            reInit();
        } catch (SOSHibernateException e) {
            LOGGER.warn("Could not reeint EventHandler after deleting jobs: " + e.getMessage());
        }

    }
}
