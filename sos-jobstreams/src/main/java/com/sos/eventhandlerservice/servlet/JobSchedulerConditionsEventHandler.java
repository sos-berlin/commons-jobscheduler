package com.sos.eventhandlerservice.servlet;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.eventhandlerservice.classes.ConditionCustomEvent;
import com.sos.eventhandlerservice.classes.Constants;
import com.sos.eventhandlerservice.classes.FileBaseRemovedEvent;
import com.sos.eventhandlerservice.classes.JobSchedulerEvent;
import com.sos.eventhandlerservice.classes.OrderFinishedEvent;
import com.sos.eventhandlerservice.classes.TaskEndEvent;
import com.sos.eventhandlerservice.db.FilterConsumedInConditions;
import com.sos.eventhandlerservice.db.FilterEvents;
import com.sos.eventhandlerservice.resolver.JSConditionResolver;
import com.sos.eventhandlerservice.resolver.JSEvent;
import com.sos.eventhandlerservice.resolver.JSEvents;
import com.sos.eventhandlerservice.resolver.JSInCondition;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.classes.event.JobSchedulerEvent.EventType;
import com.sos.jitl.classes.event.JobSchedulerPluginEventHandler;
import com.sos.jitl.classes.plugin.PluginMailer;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class JobSchedulerConditionsEventHandler extends JobSchedulerPluginEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerConditionsEventHandler.class);
    public static final String CUSTOM_EVENT_KEY = JobSchedulerConditionsEventHandler.class.getSimpleName();;
    private SOSHibernateFactory reportingFactory;
    private int waitInterval = 2;
    private String session;
    JSConditionResolver conditionResolver;

    public static enum CustomEventType {
        InconditionValidated, EventCreated
    }

    public static enum CustomEventTypeValue {
        incondition
    }

    public JobSchedulerConditionsEventHandler() {
        super();
    }

    public JobSchedulerConditionsEventHandler(SchedulerXmlCommandExecutor xmlCommandExecutor, EventPublisher eventBus) {
        super(xmlCommandExecutor, eventBus);
    }

    @Override
    public void onActivate(PluginMailer mailer) {
        super.onActivate(mailer);

        String method = "onActivate";
        session = Constants.getSession();
        SOSHibernateSession reportingSession = null;
        try {
            createReportingFactory(getSettings().getHibernateConfigurationReporting());
            Constants.settings = getSettings();

            reportingSession = reportingFactory.openStatelessSession();
            File f = new File(getSettings().getConfigDirectory() + "/private/private.conf");
            conditionResolver = new JSConditionResolver(reportingSession, f, this.getSettings());
            conditionResolver.init();

            EventType[] observedEventTypes = new EventType[] { EventType.FileBasedRemoved, EventType.TaskEnded, EventType.VariablesCustomEvent };
            start(observedEventTypes);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(String.format("%s: %s", method, e.toString()), e);
        } finally {
            if (reportingSession != null) {
                reportingSession.close();
            }
            wait(waitInterval);
        }
    }

    @Override
    public void onEmptyEvent(Long eventId) {
        String method = "onEmptyEvent";
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

        // execute(false, eventId, null);
    }

    @Override
    public void onNonEmptyEvent(Long eventId, JsonArray events) {
        String method = "onNonEmptyEvent";
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

        execute(true, eventId, events);
    }

    @Override
    public void onEnded() {
        closeRestApiClient();
        closeReportingFactory();
    }

    private void execute(boolean onNonEmptyEvent, Long eventId, JsonArray events) {
        String method = "execute";
        LOGGER.debug(String.format("%s: onNonEmptyEvent=%s, eventId=%s", method, onNonEmptyEvent, eventId));
        boolean resolveInConditions = false;

        if (!Constants.getSession().equals(this.session)) {
            try {
                this.session = Constants.getSession();
                conditionResolver.reInit();
            } catch (SOSHibernateException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }

        try {
            for (JsonValue entry : events) {
                if (entry != null) {
                    LOGGER.debug(entry.toString());
                    JobSchedulerEvent jobSchedulerEvent = new JobSchedulerEvent((JsonObject) entry);
                    FilterEvents filterEvents = null;

                    switch (jobSchedulerEvent.getType()) {

                    case "FileBasedRemoved":
                        // Remove job from condition tables.
                        FileBaseRemovedEvent fileBaseRemoveEvent = new FileBaseRemovedEvent((JsonObject) entry);
                        conditionResolver.removeJob(fileBaseRemoveEvent.getJob());
                        try {
                            conditionResolver.reInit();
                        } catch (SOSHibernateException e) {
                            LOGGER.warn("Could not reeint EventHandler after deleting jobs: " + e.getMessage());
                        }

                        break;
                    case "OrderFinished":
                        OrderFinishedEvent orderFinishedEvent = new OrderFinishedEvent((JsonObject) entry);
                        conditionResolver.checkHistoryCache(orderFinishedEvent.getJobChain(), null);
                        conditionResolver.checkHistoryCache(orderFinishedEvent.getJobChain() + "(" + orderFinishedEvent.getOrderId() + ")", null);
                        break;
                    case "TaskEnded":

                        TaskEndEvent taskEndEvent = new TaskEndEvent((JsonObject) entry);
                        conditionResolver.checkHistoryCache(taskEndEvent.getJobPath(), taskEndEvent.getReturnCode());

                        JSEvents jsNewEvents = conditionResolver.resolveOutConditions(taskEndEvent.getReturnCode(), getSettings().getSchedulerId(),
                                taskEndEvent.getJobPath());
                        for (JSEvent jsNewEvent : jsNewEvents.getListOfEvents().values()) {
                            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.EventCreated.name(), jsNewEvent.getEvent());
                        }
                        resolveInConditions = true;
                        break;

                    case "VariablesCustomEvent":
                        // {"variables":{"source":"CustomEventsUtilTest"},"TYPE":"VariablesCustomEvent","key":"InitConditionResolver","eventId":1554989954492000}
                        ConditionCustomEvent customEvent = new ConditionCustomEvent((JsonObject) entry);
                        switch (customEvent.getKey()) {

                        case "InitConditionResolver":
                            conditionResolver.reInit();
                            resolveInConditions = true;
                            break;
                        case "StartConditionResolver":
                            resolveInConditions = true;
                            break;
                        case "AddEvent":
                            filterEvents = new FilterEvents();
                            filterEvents.setSession(customEvent.getSession());
                            filterEvents.setJobStream(customEvent.getJobStream());
                            filterEvents.setEvent(customEvent.getEvent());
                            try {
                                filterEvents.setOutConditionId(Long.valueOf(customEvent.getOutConditionId()));
                            } catch (NumberFormatException e) {
                                LOGGER.warn("could not add event " + filterEvents.getEvent() + ": NumberFormatException with - " + filterEvents
                                        .getOutConditionId());
                            }

                            conditionResolver.addEvent(filterEvents);
                            break;
                        case "RemoveEvent":
                            filterEvents = new FilterEvents();

                            filterEvents.setSession(customEvent.getSession());
                            filterEvents.setEvent(customEvent.getEvent());
                            conditionResolver.removeEvent(filterEvents);
                            break;

                        case "ResetConditionResolver":

                            FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
                            filterConsumedInConditions.setJobSchedulerId(super.getSettings().getSchedulerId());
                            filterConsumedInConditions.setSession(Constants.getSession());
                            filterConsumedInConditions.setJobStream(customEvent.getJobStream());
                            filterConsumedInConditions.setJob(customEvent.getJob());

                            conditionResolver.removeConsumedInconditions(filterConsumedInConditions);

                            filterEvents = new FilterEvents();
                            filterEvents.setSession(customEvent.getSession());
                            filterEvents.setJobStream(customEvent.getJobStream());
                            conditionResolver.removeEventsFromJobStream(filterEvents);

                            break;
                        }
                        break;
                    }
                }
            }
            if (resolveInConditions) {

                final long timeStart = System.currentTimeMillis();
                List<JSInCondition> listOfValidatedInconditions = conditionResolver.resolveInConditions();
                for (JSInCondition jsInCondition : listOfValidatedInconditions) {
                    publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.InconditionValidated.name(), jsInCondition.getJob());
                }
                final long timeEnd = System.currentTimeMillis();
                LOGGER.debug("Resolving all InConditions: " + (timeEnd - timeStart) + " ms.");
            }
        } catch (

        Exception e) {
            e.printStackTrace();
            LOGGER.error(String.format("%s: %s", method, e.toString()), e);
        }

        wait(waitInterval);

    }

    private void closeReportingFactory() {
        if (reportingFactory != null) {
            reportingFactory.close();
            reportingFactory = null;
        }
    }

    private void createReportingFactory(Path configFile) throws Exception {
        reportingFactory = new SOSHibernateFactory(configFile);
        reportingFactory.setIdentifier("reporting");
        reportingFactory.setAutoCommit(false);
        reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
        reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
        reportingFactory.addClassMapping(com.sos.jitl.notification.db.DBLayer.getNotificationClassMapping());
        reportingFactory.addClassMapping(Constants.getConditionsClassMapping());
        reportingFactory.build();
    }

}
