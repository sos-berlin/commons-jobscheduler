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

import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.classes.EventDate;
import com.sos.eventhandlerservice.db.DBItemConsumedInCondition;
import com.sos.eventhandlerservice.db.DBItemEvent;
import com.sos.eventhandlerservice.db.DBItemInConditionWithCommand;
import com.sos.eventhandlerservice.db.DBItemOutConditionWithEvent;
import com.sos.eventhandlerservice.db.DBLayerConsumedInConditions;
import com.sos.eventhandlerservice.db.DBLayerEvents;
import com.sos.eventhandlerservice.db.DBLayerInConditions;
import com.sos.eventhandlerservice.db.DBLayerOutConditions;
import com.sos.eventhandlerservice.db.FilterConsumedInConditions;
import com.sos.eventhandlerservice.db.FilterEvents;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(JSConditionResolver.class);

    private JSJobInConditions jsJobInConditions;
    private JSJobOutConditions jsJobOutConditions;
    private JSEvents jsEvents;
    private SOSHibernateSession sosHibernateSession;
    private BooleanExp booleanExpression;
    private JobSchedulerRestApiClient jobSchedulerRestApiClient;
    private EventHandlerSettings settings;

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
    }

    public void reInit() throws SOSHibernateException {
        jsJobInConditions = null;
        jsJobOutConditions = null;
        jsEvents = null;
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

        initEvents();

    }

    public void initEvents() throws SOSHibernateException {
        if (jsEvents == null) {
            jsEvents = new JSEvents();
            FilterEvents filterEvents = new FilterEvents();
            //filterEvents.setSession(Constants.getSession());
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            List<DBItemEvent> listOfEvents = dbLayerEvents.getEventsList(filterEvents, 0);
            jsEvents.setListOfEvents(listOfEvents);
        }
    }

    public boolean validate(Integer taskReturnCode, IJSCondition condition) {
        JSJobConditionKey jobConditionKey = new JSJobConditionKey();
        jobConditionKey.setJob(condition.getJob());
        jobConditionKey.setMasterId(condition.getMasterId());

        String expressionValue = condition.getExpression();
        JSConditions jsConditions = new JSConditions();
        List<JSCondition> listOfConditions = jsConditions.getListOfConditions(expressionValue);
        for (JSCondition jsCondition : listOfConditions) {
            switch (jsCondition.getConditionType()) {
            case "returncode": {
                JSReturnCodeResolver returnCodeResolver = new JSReturnCodeResolver();
                if (returnCodeResolver.resolve(taskReturnCode,jsCondition.getConditionParam())) {
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
            case "event": {
                String event = jsCondition.getEventName();
         
                JSEventKey jsEventKey = new JSEventKey();
                jsEventKey.setEvent(event);
                
                EventDate eventDate = new EventDate();             
                jsEventKey.setSession(eventDate.getEventDate(jsCondition.getConditionDate()));
                JSEvent jsEvent = jsEvents.getEventByWorkFlow(jsEventKey, jsCondition.getConditionWorkflow());
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

    public JSEvents resolveOutConditions(Integer taskReturnCode, String masterId, String job) throws SOSHibernateException {
        JSJobConditionKey jobConditionKey = new JSJobConditionKey();
        JSEvents newJsEvents = new JSEvents();
        jobConditionKey.setJob(job);
        jobConditionKey.setMasterId(masterId);
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
                if (filterConsumedInConditions.getWorkflow().equals(inCondition.getWorkflow()) && (filterConsumedInConditions.getJob().equals(
                        inCondition.getJob()) || filterConsumedInConditions.getJob().isEmpty()) && inCondition.isConsumed()) {
                    LOGGER.debug(expression + " no longer consumed");
                    inCondition.setConsumed(false);
                }
            }
        }
    }

    public void removeEventsFromWorkflow(FilterEvents filter) throws SOSHibernateException {
        try {
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.deleteEventsFromWorkflow(filter);
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
        itemEvent.setWorkflow(filterEvents.getWorkflow());
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

    public Boolean eventExist(JSEventKey jsEventKey, String workflow) {
        JSEvent jsEvent = jsEvents.getEventByWorkFlow(jsEventKey, workflow);
        return jsEvent != null;
    }
}
