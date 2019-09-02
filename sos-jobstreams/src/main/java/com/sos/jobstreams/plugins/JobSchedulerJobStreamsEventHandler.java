package com.sos.jobstreams.plugins;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.eventhandler.EventMeta.EventType;
import com.sos.jitl.eventhandler.handler.LoopEventHandler;
import com.sos.jitl.eventhandler.plugin.notifier.Mailer;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jobstreams.classes.ConditionCustomEvent;
import com.sos.jobstreams.classes.Constants;
import com.sos.jobstreams.classes.DurationCalculator;
import com.sos.jobstreams.classes.JobSchedulerEvent;
import com.sos.jobstreams.classes.OrderFinishedEvent;
import com.sos.jobstreams.classes.QueuedEvents;
import com.sos.jobstreams.classes.TaskEndEvent;
import com.sos.jobstreams.db.FilterConsumedInConditions;
import com.sos.jobstreams.db.FilterEvents;
import com.sos.jobstreams.resolver.JSConditionResolver;
import com.sos.jobstreams.resolver.JSEvent;
import com.sos.jobstreams.resolver.JSInCondition;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class JobSchedulerJobStreamsEventHandler extends LoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerJobStreamsEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    public static final String CUSTOM_EVENT_KEY = JobSchedulerJobStreamsEventHandler.class.getSimpleName();;
    private SOSHibernateFactory reportingFactory;
    private QueuedEvents queuedEvents = new QueuedEvents();

    private int waitInterval = 2;
    private String session;
    JSConditionResolver conditionResolver;

    public static enum CustomEventType {
        InconditionValidated, EventCreated, EventRemoved
    }

    public static enum CustomEventTypeValue {
        incondition
    }

    public JobSchedulerJobStreamsEventHandler() {
        super();
    }

    public JobSchedulerJobStreamsEventHandler(SchedulerXmlCommandExecutor xmlCommandExecutor, EventPublisher eventBus) {
        super(xmlCommandExecutor, eventBus);
    }

    @Override
    public void onActivate(Mailer mailer) {
        LOGGER.debug("onActivate Plugin");
        LOGGER.debug("WorkingDirectory:" + System.getProperty("user.dir"));

        super.onActivate(mailer);

        String method = "onActivate";
        session = Constants.getSession();
        SOSHibernateSession sosHibernateSession = null;

        try {
            LOGGER.debug("onActivate createReportingFactory");
            createReportingFactory(getSettings().getHibernateConfigurationReporting());
            Constants.settings = getSettings();
            sosHibernateSession = reportingFactory.openStatelessSession();

            conditionResolver = new JSConditionResolver(sosHibernateSession, this.getXmlCommandExecutor(), this.getSettings());
            conditionResolver.setWorkingDirectory(System.getProperty("user.dir"));
            conditionResolver.init();
            queuedEvents = new QueuedEvents();
            
            LOGGER.debug("onActivate initEventHandler");
            LOGGER.debug("Session: " + this.session);

        } catch (Exception e) {
            conditionResolver = null;
            mailer.sendOnError("JobSchedulerConditionsEventHandler", method, e);
            LOGGER.error("%s: %s", method, e.toString(), e);
        } finally {
            if (sosHibernateSession != null) {
                sosHibernateSession.close();
            }
        }

        EventType[] observedEventTypes = new EventType[] { EventType.TaskEnded, EventType.VariablesCustomEvent, EventType.OrderFinished };
        start(observedEventTypes);
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
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s: eventId=%s", method, eventId));
        }

        execute(true, eventId, events);
    }

    @Override
    public void onProcessingEnd(Long eventId) {
        LOGGER.debug("Shutdown plugin");
        closeReportingFactory();
        LOGGER.debug("Plugin closed");
    }

    private void execute(boolean onNonEmptyEvent, Long eventId, JsonArray events) {
        String method = "execute";
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s: onNonEmptyEvent=%s, eventId=%s", method, onNonEmptyEvent, eventId));
        }
        boolean resolveInConditions = false;

        SOSHibernateSession sosHibernateSession = null;

        try {

            sosHibernateSession = reportingFactory.openStatelessSession();

            if (conditionResolver == null) {
                conditionResolver = new JSConditionResolver(sosHibernateSession, this.getXmlCommandExecutor(), this.getSettings());
                conditionResolver.init();
            } else {
                conditionResolver.setReportingSession(sosHibernateSession);
            }

            if (!Constants.getSession().equals(this.session)) {
                try {
                    this.session = Constants.getSession();
                    LOGGER.debug("Change session to: " + this.session);
                    conditionResolver.reInit();

                } catch (SOSHibernateException e) {
                    LOGGER.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }

            for (JsonValue entry : events) {
                if (entry != null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(entry.toString());
                    }
                    JobSchedulerEvent jobSchedulerEvent = new JobSchedulerEvent((JsonObject) entry);
                    FilterEvents filterEvents = null;

                    switch (jobSchedulerEvent.getType()) {
                  
                    case "OrderFinished":
                        LOGGER.debug("OrderFinished event to be executed");
                        OrderFinishedEvent orderFinishedEvent = new OrderFinishedEvent((JsonObject) entry);
                        conditionResolver.checkHistoryCache(orderFinishedEvent.getJobChain(), null);
                        conditionResolver.checkHistoryCache(orderFinishedEvent.getJobChain() + "(" + orderFinishedEvent.getOrderId() + ")", null);
                        break;
                    case "TaskEnded":
                        TaskEndEvent taskEndEvent = new TaskEndEvent((JsonObject) entry);
                        LOGGER.debug("TaskEnded event to be executed:" + taskEndEvent.getTaskId() + " " + taskEndEvent.getJobPath());
                        conditionResolver.checkHistoryCache(taskEndEvent.getJobPath(), taskEndEvent.getReturnCode());

                        conditionResolver.resolveOutConditions(taskEndEvent.getReturnCode(), getSettings().getSchedulerId(), taskEndEvent
                                .getJobPath());
                        queuedEvents.handleAddEventlistBuffer(conditionResolver.getNewJsEvents());
                        queuedEvents.handleAddEventlistBuffer(conditionResolver.getRemoveJsEvents());

                        for (JSEvent jsNewEvent : conditionResolver.getNewJsEvents().getListOfEvents().values()) {
                            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.EventCreated.name(), jsNewEvent.getEvent());
                        }
                        for (JSEvent jsNewEvent : conditionResolver.getRemoveJsEvents().getListOfEvents().values()) {
                            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.EventRemoved.name(), jsNewEvent.getEvent());
                        }
                        resolveInConditions = true;
                        break;

                    case "VariablesCustomEvent":
                        // {"variables":{"source":"CustomEventsUtilTest"},"TYPE":"VariablesCustomEvent","key":"InitConditionResolver","eventId":1554989954492000}
                        ConditionCustomEvent customEvent = new ConditionCustomEvent((JsonObject) entry);
                        switch (customEvent.getKey()) {

                        case "InitConditionResolver":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + "InitConditionResolver");
                            conditionResolver.reInit();
                            resolveInConditions = true;
                            break;
                        case "StartConditionResolver":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + "StartConditionResolver");
                            resolveInConditions = true;
                            break;
                        case "AddEvent":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + "AddEvent --> " + customEvent.getEvent());

                            JSEvent event = new JSEvent();
                            event.setCreated(new Date());
                            event.setEvent(customEvent.getEvent());
                            event.setSession(customEvent.getSession());
                            event.setJobStream(customEvent.getJobStream());
                            event.setSchedulerId(super.getSettings().getSchedulerId());

                            try {
                                event.setOutConditionId(Long.valueOf(customEvent.getOutConditionId()));
                            } catch (NumberFormatException e) {
                                LOGGER.warn("could not add event " + event.getEvent() + ": NumberFormatException with -> " + event
                                        .getOutConditionId());
                            }

                            conditionResolver.addEvent(event);
                            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.EventCreated.name(), customEvent.getEvent());
                            queuedEvents.handleAddEventlistBuffer(conditionResolver.getNewJsEvents());

                            break;
                        case "RemoveEvent":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + "RemoveEvent -->" + customEvent.getEvent());

                            event = new JSEvent();
                            event.setCreated(new Date());
                            event.setEvent(customEvent.getEvent());
                            event.setSession(customEvent.getSession());
                            event.setSchedulerId(super.getSettings().getSchedulerId());

                            conditionResolver.removeEvent(event);
                            queuedEvents.handleAddEventlistBuffer(conditionResolver.getNewJsEvents());
                            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.EventRemoved.name(), customEvent.getEvent());

                            break;

                        case "ResetConditionResolver":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + "ResetConditionResolver -->" + customEvent.getJobStream());

                            FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
                            filterConsumedInConditions.setJobSchedulerId(super.getSettings().getSchedulerId());
                            filterConsumedInConditions.setSession(Constants.getSession());
                            filterConsumedInConditions.setJobStream(customEvent.getJobStream());
                            filterConsumedInConditions.setJob(customEvent.getJob());

                            conditionResolver.removeConsumedInconditions(filterConsumedInConditions);

                            filterEvents = new FilterEvents();
                            filterEvents.setSchedulerId(getSettings().getSchedulerId());
                            filterEvents.setSession(customEvent.getSession());
                            filterEvents.setJob(customEvent.getJob());
                            filterEvents.setJobStream(customEvent.getJobStream());
                            conditionResolver.removeEventsFromJobStream(filterEvents);

                            break;
                        }
                        break;
                    default:
                        LOGGER.debug(jobSchedulerEvent.getType() + " skipped");
                    }
                }
            }
            if (resolveInConditions) {

                DurationCalculator duration = null;
                if (isDebugEnabled) {
                    LOGGER.debug("Resolve inconditinons");
                    duration = new DurationCalculator();
                }
                List<JSInCondition> listOfValidatedInconditions = conditionResolver.resolveInConditions();
                for (JSInCondition jsInCondition : listOfValidatedInconditions) {
                    publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.InconditionValidated.name(), jsInCondition.getJob());
                }
                if (isDebugEnabled & duration != null) {
                    duration.end("Resolving all InConditions: ");
                }
            }
        } catch (Exception e) {
            this.getMailer().sendOnError("JobSchedulerConditionsEventHandler", method, e);
            LOGGER.error(String.format("%s: %s", method, e.toString()), e);
        } finally {
            if (sosHibernateSession != null) {
                sosHibernateSession.close();
            }
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
        reportingFactory.setIdentifier(getIdentifier());
        reportingFactory.setAutoCommit(false);
        reportingFactory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        reportingFactory.addClassMapping(DBLayer.getReportingClassMapping());
        reportingFactory.addClassMapping(DBLayer.getInventoryClassMapping());
        reportingFactory.addClassMapping(com.sos.jitl.notification.db.DBLayer.getNotificationClassMapping());
        reportingFactory.addClassMapping(Constants.getConditionsClassMapping());
        reportingFactory.build();
    }

}