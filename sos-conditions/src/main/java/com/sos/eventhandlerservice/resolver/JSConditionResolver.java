package com.sos.eventhandlerservice.resolver;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.mapping.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.eventhandlerservice.classes.TaskEndEvent;
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

    public JSConditionResolver(SOSHibernateSession sosHibernateSession) throws UnsupportedEncodingException, InterruptedException, SOSException,
            URISyntaxException {
        super();
        booleanExpression = new BooleanExp("");
        this.sosHibernateSession = sosHibernateSession;
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider();
        WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken(null);

        jobSchedulerRestApiClient = new JobSchedulerRestApiClient();
        jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
        jobSchedulerRestApiClient.addHeader("Accept", "application/json");
        jobSchedulerRestApiClient.addHeader("X-ACCESS-TOKEN", webserviceCredentials.getAccessToken());
    }

    public JSConditionResolver(SOSHibernateSession sosHibernateSession, File privateConf) throws UnsupportedEncodingException, InterruptedException,
            SOSException, URISyntaxException {
        super();
        booleanExpression = new BooleanExp("");
        this.sosHibernateSession = sosHibernateSession;
        AccessTokenProvider accessTokenProvider = new AccessTokenProvider(privateConf.getAbsolutePath());
        WebserviceCredentials webserviceCredentials = accessTokenProvider.getAccessToken(null);

        jobSchedulerRestApiClient = new JobSchedulerRestApiClient();
        jobSchedulerRestApiClient.addHeader("Content-Type", "application/json");
        jobSchedulerRestApiClient.addHeader("Accept", "application/json");
        jobSchedulerRestApiClient.addHeader("X-ACCESS-TOKEN", webserviceCredentials.getAccessToken());
    }

    public JSConditionResolver(SOSHibernateSession sosHibernateSession, String accessToken) throws UnsupportedEncodingException, InterruptedException,
            SOSException, URISyntaxException {
        super();
        booleanExpression = new BooleanExp("");
        this.sosHibernateSession = sosHibernateSession;

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
            jsJobInConditions = new JSJobInConditions();
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
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            List<DBItemEvent> listOfEvents = dbLayerEvents.getEventsList(filterEvents, 0);
            jsEvents.setListOfEvents(listOfEvents);
        }
    }

    public boolean validate(TaskEndEvent taskEndEvent, IJSCondition condition) {
        JSJobConditionKey jobConditionKey = new JSJobConditionKey();
        jobConditionKey.setJob(condition.getJob());
        jobConditionKey.setMasterId(condition.getMasterId());

        String expressionValue = condition.getExpression();
        JSConditions jsConditions = new JSConditions();
        List<JSCondition> listOfConditions = jsConditions.getListOfConditions(expressionValue);
        for (JSCondition jsCondition : listOfConditions) {
            switch (jsCondition.getConditionType()) {
            case "returncode": {
                Integer returncode = jsCondition.getConditionIntegerParam();
                if (taskEndEvent != null && returncode != null && returncode.equals(taskEndEvent.getReturnCode())) {
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
                String event = jsCondition.getConditionParam();
                event = event.replace(jsCondition.getConditionWorkflow() + ".", "");
                JSEventKey jsEventKey = new JSEventKey();
                jsEventKey.setEvent(event);
                jsEventKey.setSession("now");
                JSEvent jsEvent = jsEvents.getEvent(jsEventKey);
                if (jsEvent != null) {
                    if (jsCondition.getConditionWorkflow().isEmpty() || jsCondition.getConditionWorkflow().equals(jsEvent.getWorkflow())) {
                        expressionValue = expressionValue.replace(jsCondition.getConditionType() + ":" + jsCondition.getConditionParam(), "true");
                        expressionValue = expressionValue.replace(jsCondition.getConditionParam(), "true");
                    }
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

    public List<String> resolveInConditions() throws UnsupportedEncodingException, MalformedURLException, InterruptedException, SOSException,
            URISyntaxException {
        List<String> listOfWorkflows = new ArrayList<String>();
        for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
            for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                String expression = inCondition.getJob() + ":" + inCondition.getExpression();

                System.out.println("---InCondition: " + expression);
                if (validate(null, inCondition)) {
                    if (!inCondition.isConsumed()) {
                        inCondition.executeCommands(sosHibernateSession, jobSchedulerRestApiClient);
                        if (!listOfWorkflows.contains(inCondition.getWorkflow())) {
                            listOfWorkflows.add(inCondition.getWorkflow());
                        }
                    } else {
                        System.out.println(inCondition.getExpression() + "-->already consumed");
                    }
                } else {
                    System.out.println(expression + "-->false");
                }
                System.out.println("");
            }
        }
        return listOfWorkflows;
    }

    public void resolveOutConditions(TaskEndEvent taskEndEvent, String masterId, String job) throws SOSHibernateException {
        JSJobConditionKey jobConditionKey = new JSJobConditionKey();
        jobConditionKey.setJob(job);
        jobConditionKey.setMasterId(masterId);
        JSOutConditions jobOutConditions = jsJobOutConditions.getListOfJobOutConditions().get(jobConditionKey);

        if (jobOutConditions != null) {
            for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                String expression = outCondition.getJob() + ":" + outCondition.getExpression();

                System.out.println("---OutCondition: " + expression);
                if (validate(taskEndEvent, outCondition)) {
                    System.out.println("create events ------>");
                    outCondition.storeOutConditionEvents(sosHibernateSession, jsEvents);

                } else {
                    System.out.println(expression + "-->false");
                }
                System.out.println("");
            }
        }
    }

    public void resolveOutConditions() {
        for (JSOutConditions jobOutConditions : jsJobOutConditions.getListOfJobOutConditions().values()) {
            for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                String expression = outCondition.getJob() + ":" + outCondition.getExpression();

                System.out.println("---OutCondition: " + expression);
                if (validate(null, outCondition)) {
                    System.out.println(expression + "-->true");
                } else {
                    System.out.println(expression + "-->false");
                }
                System.out.println("");
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
            sosHibernateSession.rollback();
        }

        for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
            for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                String expression = inCondition.getJob() + ":" + inCondition.getExpression();
                if (filterConsumedInConditions.getWorkflow().equals(inCondition.getWorkflow()) && (filterConsumedInConditions.getJob().equals(
                        inCondition.getJob()) || filterConsumedInConditions.getJob().isEmpty()) && inCondition.isConsumed()) {
                    System.out.println(expression + " no longer consumed");
                    inCondition.setConsumed(false);
                }
            }
        }

    }

    public boolean eventExist(JSEventKey jsEventKey, String workflow) {
        JSEvent jsEvent = jsEvents.getEvent(jsEventKey);
        return jsEvent != null && (workflow == null || workflow.isEmpty() || jsEvent.getWorkflow().equals(workflow));
    }

    public JSEvents getJsEvents() {
        return jsEvents;
    }
}
