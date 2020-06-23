package com.sos.jobstreams.plugins;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.icu.util.TimeZone;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.exceptions.SOSHibernateOpenSessionException;
import com.sos.jitl.eventhandler.EventMeta.EventType;
import com.sos.jitl.eventhandler.handler.LoopEventHandler;
import com.sos.jitl.eventhandler.plugin.notifier.Notifier;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jitl.jobstreams.classes.JSEvent;
import com.sos.jitl.jobstreams.db.DBItemJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarter;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamStarters;
import com.sos.jitl.jobstreams.db.FilterConsumedInConditions;
import com.sos.jitl.jobstreams.db.FilterEvents;
import com.sos.jitl.jobstreams.db.FilterJobStreamHistory;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jobstreams.classes.ConditionCustomEvent;
import com.sos.jobstreams.classes.DurationCalculator;
import com.sos.jobstreams.classes.JobSchedulerEvent;
import com.sos.jobstreams.classes.JobStarterOptions;
import com.sos.jobstreams.classes.OrderFinishedEvent;
import com.sos.jobstreams.classes.QueuedEvents;
import com.sos.jobstreams.classes.TaskEndEvent;
import com.sos.jobstreams.resolver.JSConditionResolver;
import com.sos.jobstreams.resolver.JSHistoryEntry;
import com.sos.jobstreams.resolver.JSInCondition;
import com.sos.jobstreams.resolver.JSInConditionCommand;
import com.sos.jobstreams.resolver.JSJobStream;
import com.sos.jobstreams.resolver.JSJobStreamStarter;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.instances.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class JobSchedulerJobStreamsEventHandler extends LoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerJobStreamsEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    public static final String CUSTOM_EVENT_KEY = JobSchedulerJobStreamsEventHandler.class.getSimpleName();;
    private SOSHibernateFactory reportingFactory;
    private QueuedEvents addQueuedEvents;
    private QueuedEvents delQueuedEvents;
    private Timer globalEventsPollTimer;
    private Timer nextJobStartTimer;
    private JSJobStreamStarter nextStarter;
    private int waitInterval = 2;
    private String session;
    JSConditionResolver conditionResolver;

    public static enum CustomEventType {
        InconditionValidated, EventCreated, EventRemoved, JobStreamRemoved, StartTime
    }

    public static enum CustomEventTypeValue {
        incondition

    }

    public void removeTimer() {
        if (isDebugEnabled) {
            LOGGER.debug("Polling for is disabled");
        }
        if (globalEventsPollTimer != null) {
            globalEventsPollTimer.cancel();
            globalEventsPollTimer.purge();
            globalEventsPollTimer = null;
        }
    }

    public void resetGlobalEventsPollTimer() {
        if (globalEventsPollTimer != null) {
            globalEventsPollTimer.cancel();
            globalEventsPollTimer.purge();
        }
        globalEventsPollTimer = new Timer();
        globalEventsPollTimer.schedule(new GlobalPollingTask(), 60 * 1000, 60 * 1000);
        if (isDebugEnabled) {
            LOGGER.debug("60s Polling for is activated");
        }
    }

    public void resetNextJobStartTimer(SOSHibernateSession sosHibernateSession) {
        if (nextJobStartTimer != null) {
            nextJobStartTimer.cancel();
            nextJobStartTimer.purge();
        }
        nextJobStartTimer = new Timer();
        Long delay = -1L;

        do {
            nextStarter = this.conditionResolver.getNextStarttime();

            if (nextStarter != null) {
                DateTime nextDateTime = new DateTime(nextStarter.getNextStartFromList());
                Long next = nextDateTime.getMillis();
                Long now = new Date().getTime();
                delay = next - now;
                if (delay > 0) {
                    LOGGER.debug("Next start:" + nextStarter.getAllJobNames() + " at " + nextStarter.getNextStart());
                    try {
                        if (sosHibernateSession != null) {

                            DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
                            sosHibernateSession.beginTransaction();
                            DBItemJobStreamStarter dbItemJobStreamStarter = nextStarter.getItemJobStreamStarter();
                            // Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            // calendar.setTimeInMillis(UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC",
                            // nextDateTime).getTime());
                            // dbItemJobStreamStarter.setNextStart(calendar.getTime());
                            dbItemJobStreamStarter.setNextStart(new Date(next));
                            dbLayerJobStreamStarters.update(dbItemJobStreamStarter);
                            sosHibernateSession.commit();
                            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.StartTime.name(), String.valueOf(nextStarter
                                    .getItemJobStreamStarter().getId()));

                        }
                    } catch (SOSHibernateException e) {
                        LOGGER.error("Could not update next start time for starter: " + nextStarter.getItemJobStreamStarter().getId() + ":"
                                + nextStarter.getItemJobStreamStarter().getTitle(), e);
                    }

                    nextJobStartTimer.schedule(new JobStartTask(), delay);
                } else {
                    LOGGER.debug("negative delay");
                }
            }
        } while (delay < 0 && nextStarter != null);
        if (nextStarter == null) {
            LOGGER.info("no more start times found");
        }

    }

    public class GlobalPollingTask extends TimerTask {

        public void run() {
            SOSHibernateSession sosHibernateSession;
            try {
                sosHibernateSession = reportingFactory.openStatelessSession();
                try {
                    if (conditionResolver.haveGlobalEvents()) {
                        conditionResolver.reInitEvents(sosHibernateSession);
                    }
                    resolveInConditions(sosHibernateSession);
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.error("Timer Task Error", e);
                    removeTimer();
                } finally {
                    if (sosHibernateSession != null) {
                        sosHibernateSession.close();
                    }
                }
            } catch (SOSHibernateOpenSessionException e1) {
                LOGGER.warn("Could not create sosHibernateSession");
            }
        }
    }

    public class JobStartTask extends TimerTask {

        private static final String ACTIVE = "active";

        public void run() {

            SOSHibernateSession sosHibernateSession = null;
            try {
                sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:JobStartTask.run");
            } catch (SOSHibernateOpenSessionException e) {
                LOGGER.error("Could not initiate session", e);
            }

            try {
                LOGGER.debug("Start of jobs ==>" + nextStarter.getAllJobNames() + " at " + nextStarter.getNextStart());
                nextStarter.setLastStart();

                if (ACTIVE.equals(nextStarter.getItemJobStreamStarter().getState())) {
                    startJobs(nextStarter);
                }
                if (nextStarter.getNextStartFromList() == null) {
                    nextStarter.schedule();
                    DateTime nextDateTime = new DateTime(nextStarter.getNextStartFromList());

                    // Long next = UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC", nextDateTime).getTime();
                    Long now = new Date().getTime();
                    Long next = nextDateTime.getMillis();
 
                    // Long now = new Date().getTime();
                    Long delay = next - now;

                    if (delay > 0) {
                        LOGGER.debug("Recalculated next start:" + nextStarter.getAllJobNames() + " at " + nextStarter.getNextStart());

                        if (sosHibernateSession != null) {

                            DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
                            sosHibernateSession.beginTransaction();
                            DBItemJobStreamStarter dbItemJobStreamStarter = nextStarter.getItemJobStreamStarter();
                            // Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            // calendar.setTimeInMillis(UtcTimeHelper.convertTimeZonesToDate(UtcTimeHelper.localTimeZoneString(), "UTC",
                            // nextDateTime).getTime());
                            // dbItemJobStreamStarter.setNextStart(calendar.getTime());
                            dbItemJobStreamStarter.setNextStart(new Date(next));
                            dbLayerJobStreamStarters.update(dbItemJobStreamStarter);
                            sosHibernateSession.commit();
                            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.StartTime.name(), String.valueOf(nextStarter
                                    .getItemJobStreamStarter().getId()));
                        }
                    }
                }
                resetNextJobStartTimer(sosHibernateSession);

            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("Timer Task Error", e);
                resetNextJobStartTimer(sosHibernateSession);
            } finally {
                if (sosHibernateSession != null) {
                    sosHibernateSession.close();
                }
            }
        }
    }

    private void startJobs(JSJobStreamStarter jobStreamStarter) throws Exception {
        List<JobStarterOptions> listOfStartedJobs = jobStreamStarter.startJobs(getXmlCommandExecutor());
        UUID uuid = UUID.randomUUID();
        SOSHibernateSession sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:resolveInCondtions");
        sosHibernateSession.beginTransaction();
        try {

            DBItemJobStreamHistory dbItemJobStreamHistory = new DBItemJobStreamHistory();
            dbItemJobStreamHistory.setSchedulerId(super.getSettings().getSchedulerId());
            dbItemJobStreamHistory.setContextId(uuid.toString());
            dbItemJobStreamHistory.setCreated(new Date());
            dbItemJobStreamHistory.setJobStream(jobStreamStarter.getItemJobStreamStarter().getJobStream());
            dbItemJobStreamHistory.setJobStreamStarter(jobStreamStarter.getItemJobStreamStarter().getId());
            dbItemJobStreamHistory.setRunning(true);
            dbItemJobStreamHistory.setStarted(new Date());

            JSHistoryEntry historyEntry = new JSHistoryEntry();
            historyEntry.setCreated(new Date());
            historyEntry.setItemJobStreamHistory(dbItemJobStreamHistory);
            conditionResolver.addParameters(uuid, jobStreamStarter.getListOfParameters());

            JSJobStream jobStream = conditionResolver.getJsJobStreams().getJobStream(jobStreamStarter.getItemJobStreamStarter().getJobStream());
            LOGGER.debug(String.format("Adding history entry with context-id %s to jobStream %s", dbItemJobStreamHistory.getContextId(), jobStream
                    .getJobStream()));
            jobStream.getJsHistory().addHistoryEntry(historyEntry, sosHibernateSession);

            for (JobStarterOptions startedJob : listOfStartedJobs) {
                conditionResolver.getJobStreamContexts().addTaskToContext(uuid, super.getSettings().getSchedulerId(), startedJob,
                        sosHibernateSession);
            }
            LOGGER.debug(jobStreamStarter.getAllJobNames() + " started");
            sosHibernateSession.commit();
            conditionResolver.getListOfHistoryIds().put(uuid, historyEntry.getItemJobStreamHistory());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            sosHibernateSession.rollback();
        } finally {
            sosHibernateSession.close();
        }
    }

    public JobSchedulerJobStreamsEventHandler() {
        super();
        addQueuedEvents = new QueuedEvents();
        delQueuedEvents = new QueuedEvents();
    }

    public JobSchedulerJobStreamsEventHandler(SchedulerXmlCommandExecutor xmlCommandExecutor, EventPublisher eventBus) {
        super(xmlCommandExecutor, eventBus);
        addQueuedEvents = new QueuedEvents();
        delQueuedEvents = new QueuedEvents();
    }

    @Override
    public void onActivate(Notifier notifier) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        LOGGER.debug("onActivate Plugin");
        LOGGER.debug("WorkingDirectory:" + System.getProperty("user.dir"));
        LOGGER.debug("TimeZone: " + TimeZone.getDefault().getID());

        super.onActivate(notifier);
        String method = "onActivate";
        session = Constants.getSession();
        SOSHibernateSession sosHibernateSession = null;

        try {
            LOGGER.debug("onActivate createReportingFactory");
            LOGGER.debug("onActivate openSession");
            createReportingFactory(getSettings().getHibernateConfigurationReporting());
            sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:onActivate");

            if (getSettings().getTimezone() == null) {
                getSettings().setTimezone(timeZoneFromInstances(sosHibernateSession));
            }
            Constants.settings = getSettings();

            Constants.baseUrl = this.getBaseUrl();

            conditionResolver = new JSConditionResolver(this.getXmlCommandExecutor(), this.getSettings());
            conditionResolver.setWorkingDirectory(System.getProperty("user.dir"));
            LOGGER.debug("onActivate init condition resolver");
            conditionResolver.init(sosHibernateSession);
            LOGGER.debug("onActivate reset timers");
            this.resetGlobalEventsPollTimer();
            this.resetNextJobStartTimer(sosHibernateSession);

            LOGGER.debug("onActivate initEventHandler");

            EventType[] observedEventTypes = new EventType[] { EventType.TaskClosed, EventType.TaskEnded, EventType.VariablesCustomEvent,
                    EventType.OrderFinished };
            LOGGER.debug("Session: " + this.session);
            sosHibernateSession.close();
            start(observedEventTypes);

        } catch (Exception e) {
            conditionResolver = null;
            getNotifier().notifyOnError(method, e);
            LOGGER.error(String.format("%s: %s", method, e.toString()), e);
        } finally {
            if (sosHibernateSession != null) {
                sosHibernateSession.close();
            }
        }
    }

    @Override
    public void onEmptyEvent(Long eventId) {
        String method = "onEmptyEvent";
        LOGGER.debug(String.format("%s: eventId=%s", method, eventId));

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

    private void performTaskEnd(SOSHibernateSession sosHibernateSession, TaskEndEvent taskEndEvent) throws Exception {
        LOGGER.debug("TaskEnded event to be executed:" + taskEndEvent.getTaskId() + " " + taskEndEvent.getJobPath());
        conditionResolver.checkHistoryCache(sosHibernateSession, taskEndEvent.getJobPath(), taskEndEvent.getReturnCode());

        boolean dbChange = conditionResolver.resolveOutConditions(sosHibernateSession, taskEndEvent, getSettings().getSchedulerId(), taskEndEvent
                .getJobPath());
        UUID contextId = conditionResolver.getJobStreamContexts().getContext(taskEndEvent.getTaskIdLong());
        conditionResolver.enableInconditionsForJob(getSettings().getSchedulerId(), taskEndEvent.getJobPath(), contextId);

        for (JSEvent jsNewEvent : conditionResolver.getNewJsEvents().getListOfEvents().values()) {
            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.EventCreated.name(), jsNewEvent.getEvent());
        }
        for (JSEvent jsNewEvent : conditionResolver.getRemoveJsEvents().getListOfEvents().values()) {
            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.EventRemoved.name(), jsNewEvent.getEvent());
        }
        if (!conditionResolver.getNewJsEvents().isEmpty() || !conditionResolver.getRemoveJsEvents().isEmpty()) {
            boolean reinint = false;
            addQueuedEvents.handleEventlistBuffer(conditionResolver.getNewJsEvents());
            if (dbChange && !this.addQueuedEvents.isEmpty()) {
                this.addQueuedEvents.storetoDb(sosHibernateSession, conditionResolver.getJsEvents());
                reinint = true;
            }
            delQueuedEvents.handleEventlistBuffer(conditionResolver.getRemoveJsEvents());
            if (dbChange && !this.delQueuedEvents.isEmpty()) {
                this.addQueuedEvents.deleteFromDb(sosHibernateSession, conditionResolver.getJsEvents());
                reinint = true;
            }
            if (reinint) {
                conditionResolver.reInitConsumedInConditions(sosHibernateSession);
            }
        }
    }

    private String timeZoneFromInstances(SOSHibernateSession sosHibernateSesssion) throws DBInvalidDataException, DBConnectionRefusedException {
        InventoryInstancesDBLayer inventoryInstancesDBLayer = new InventoryInstancesDBLayer(sosHibernateSesssion);

        List<DBItemInventoryInstance> l = inventoryInstancesDBLayer.getInventoryInstancesBySchedulerId(getSettings().getSchedulerId());
        if (l.size() > 0) {
            return l.get(0).getTimeZone();
        } else {
            return "Europe/Berlin";
        }
    }

    private void resolveInConditions(SOSHibernateSession sosHibernateSession) throws Exception {
        DurationCalculator duration = null;
        if (isDebugEnabled) {
            LOGGER.debug("Resolve in-conditions");
            duration = new DurationCalculator();
        }

        try {

            boolean skippedTask = false;
            do {
                skippedTask = false;

                List<JSInCondition> listOfValidatedInconditions = conditionResolver.resolveInConditions(sosHibernateSession, super.getSettings()
                        .getSchedulerId());
                if (listOfValidatedInconditions != null) {
                    for (JSInCondition jsInCondition : listOfValidatedInconditions) {
                        LOGGER.debug("checking whether to execute out conditions for skipped jobs");
                        LOGGER.debug("isSkipOutCondition:" + jsInCondition.isSkipOutCondition());
                        publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.InconditionValidated.name(), jsInCondition.getJob());
                        if (!jsInCondition.isSkipOutCondition()) {
                            for (JSInConditionCommand inConditionCommand : jsInCondition.getListOfInConditionCommand()) {
                                LOGGER.debug("isExecuted:" + inConditionCommand.getCommand() + "-->" + inConditionCommand.isExecuted());
                                if (!inConditionCommand.isExecuted()) {
                                    TaskEndEvent taskEndEvent = new TaskEndEvent();
                                    taskEndEvent.setJobPath(jsInCondition.getJob());
                                    taskEndEvent.setReturnCode(0);
                                    taskEndEvent.setTaskId("");
                                    taskEndEvent.setEvaluatedContextId(jsInCondition.getEvaluatedContextId());
                                    LOGGER.debug(String.format("Job %s skipped. Job run will be simulated with rc=0", jsInCondition.getJob()));
                                    inConditionCommand.setExecuted(true);
                                    performTaskEnd(sosHibernateSession, taskEndEvent);
                                    skippedTask = true;
                                }
                            }
                        }
                    }
                }
            } while (skippedTask);
            if (isDebugEnabled & duration != null) {
                duration.end("Resolving all InConditions: ");
            }

        } catch (Exception e) {
            LOGGER.error("Could not resolve In Conditions", e);
        }
    }

    private void execute(boolean onNonEmptyEvent, Long eventId, JsonArray events) {
        String method = "execute";
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s: onNonEmptyEvent=%s, eventId=%s", method, onNonEmptyEvent, eventId));
        }
        boolean resolveInConditions = false;

        SOSHibernateSession sosHibernateSession = null;

        try {

            sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:execute");

            if (conditionResolver == null) {
                sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:execute");
                conditionResolver = new JSConditionResolver(this.getXmlCommandExecutor(), this.getSettings());
                try {
                    conditionResolver.init(sosHibernateSession);
                    this.resetGlobalEventsPollTimer();
                    this.resetNextJobStartTimer(sosHibernateSession);

                } catch (Exception e) {
                    conditionResolver = null;
                    throw e;
                }
            }

            if (!Constants.getSession().equals(this.session)) {
                try {
                    this.session = Constants.getSession();
                    LOGGER.debug("Change session to: " + this.session);
                    conditionResolver.reInit(sosHibernateSession);

                } catch (SOSHibernateException e) {
                    conditionResolver = null;
                    getNotifier().smartNotifyOnError(getClass(), e);
                    LOGGER.error(String.format("%s: %s", method, e.toString()), e);
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
                        LOGGER.debug("Event event to be executed: " + jobSchedulerEvent.getType());
                        OrderFinishedEvent orderFinishedEvent = new OrderFinishedEvent((JsonObject) entry);
                        conditionResolver.checkHistoryCache(sosHibernateSession, orderFinishedEvent.getJobChain(), null);
                        conditionResolver.checkHistoryCache(sosHibernateSession, orderFinishedEvent.getJobChain() + "(" + orderFinishedEvent
                                .getOrderId() + ")", null);
                        break;
                    case "TaskEnded":
                        LOGGER.debug("Event event to be executed: " + jobSchedulerEvent.getType());
                        TaskEndEvent taskEndEvent = new TaskEndEvent((JsonObject) entry);
                        performTaskEnd(sosHibernateSession, taskEndEvent);
                        resolveInConditions = true;
                        break;
                    case "TaskClosed":
                        // taskEndEvent = new TaskEndEvent((JsonObject) entry);
                        // UUID contextId = conditionResolver.getJobStreamContexts().getContext(taskEndEvent.getTaskIdLong());
                        // conditionResolver.enableInconditionsForJob(getSettings().getSchedulerId(), taskEndEvent.getJobPath(), contextId);
                        break;

                    case "VariablesCustomEvent":
                        // {"variables":{"source":"CustomEventsUtilTest"},"TYPE":"VariablesCustomEvent","key":"InitConditionResolver","eventId":1554989954492000}
                        ConditionCustomEvent customEvent = new ConditionCustomEvent((JsonObject) entry);
                        switch (customEvent.getKey()) {

                        case "InitConditionResolver":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            conditionResolver.reInit(sosHibernateSession);
                            this.resetGlobalEventsPollTimer();
                            resolveInConditions = true;
                            resetNextJobStartTimer(sosHibernateSession);
                            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.StartTime.name(), "");
                            break;
                        case "StartJobStream":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            JSJobStreamStarter jsJobStreamStarter = conditionResolver.getListOfJobStreamStarter().get(customEvent
                                    .getJobStreamStarterId());
                            if (jsJobStreamStarter != null) {
                                startJobs(jsJobStreamStarter);
                            } else {
                                LOGGER.warn("Could not find JobStream starter with id: " + customEvent.getJobStreamStarterId());
                            }
                            this.resetGlobalEventsPollTimer();

                            break;
                        case "CalendarUsageUpdated":
                        case "CalendarDeleted":

                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            conditionResolver.reInit(sosHibernateSession);
                            conditionResolver.reinitCalendarUsage(sosHibernateSession);

                            this.resetGlobalEventsPollTimer();
                            resetNextJobStartTimer(sosHibernateSession);
                            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.StartTime.name(), "");

                            resolveInConditions = true;
                            break;
                        case "StartConditionResolver":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            conditionResolver.reInitEvents(sosHibernateSession);
                            resolveInConditions = true;
                            break;

                        case "AddEvent":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey() + " --> " + customEvent.getEvent());

                            DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);
                            FilterJobStreamHistory filterJobStreamHistory = new FilterJobStreamHistory();
                            filterJobStreamHistory.setContextId(customEvent.getSession());
                            List<DBItemJobStreamHistory> l = dbLayerJobStreamHistory.getJobStreamHistoryList(filterJobStreamHistory, 0);
                            if (l.size() <= 0) {
                                LOGGER.warn("Could not add Event " + customEvent.getEvent() + " as session " + customEvent.getSession()
                                        + " has not been found");
                            } else {
                                JSEvent event = new JSEvent();
                                event.setCreated(new Date());
                                event.setEvent(customEvent.getEvent());
                                event.setJobStream(customEvent.getJobStream());
                                event.setSchedulerId(super.getSettings().getSchedulerId());
                                event.setGlobalEvent(customEvent.isGlobalEvent());
                                event.setJobStreamHistoryId(l.get(0).getId());
                                if (customEvent.isGlobalEvent()) {
                                    event.setSession(Constants.getSession());
                                } else {
                                    event.setSession(customEvent.getSession());
                                }

                                try {
                                    event.setOutConditionId(Long.valueOf(customEvent.getOutConditionId()));
                                } catch (NumberFormatException e) {
                                    LOGGER.warn("could not add event " + event.getEvent() + ": NumberFormatException with -> " + event
                                            .getOutConditionId());
                                }

                                conditionResolver.addEvent(sosHibernateSession, event);
                                if (!customEvent.isGlobalEvent()) {
                                    event.setSession(Constants.getSession());
                                    conditionResolver.addEvent(sosHibernateSession, event);
                                }

                                publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.EventCreated.name(), customEvent.getEvent());
                                addQueuedEvents.handleEventlistBuffer(conditionResolver.getNewJsEvents());
                                if (!conditionResolver.getNewJsEvents().isEmpty() && !this.addQueuedEvents.isEmpty()) {
                                    this.addQueuedEvents.storetoDb(sosHibernateSession, conditionResolver.getJsEvents());
                                    conditionResolver.reInitConsumedInConditions(sosHibernateSession);
                                }
                            }

                            break;
                        case "RemoveEvent":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey() + " --> " + customEvent.getEvent());
                            JSEvent event = new JSEvent();
                            event.setCreated(new Date());
                            event.setEvent(customEvent.getEvent());
                            event.setJobStream(customEvent.getJobStream());

                            if (customEvent.isGlobalEvent()) {
                                event.setSession(Constants.getSession());
                            } else {
                                event.setSession(customEvent.getSession());
                            }

                            event.setSchedulerId(super.getSettings().getSchedulerId());
                            event.setGlobalEvent(customEvent.isGlobalEvent());

                            conditionResolver.removeEvent(sosHibernateSession, event);
                            if (!customEvent.isGlobalEvent()) {
                                event.setSession(Constants.getSession());
                                conditionResolver.removeEvent(sosHibernateSession, event);
                            }

                            delQueuedEvents.handleEventlistBuffer(conditionResolver.getRemoveJsEvents());
                            if (!conditionResolver.getRemoveJsEvents().isEmpty() && !this.delQueuedEvents.isEmpty()) {
                                this.addQueuedEvents.deleteFromDb(sosHibernateSession, conditionResolver.getJsEvents());
                                conditionResolver.reInitConsumedInConditions(sosHibernateSession);
                            }
                            publishCustomEvent(CUSTOM_EVENT_KEY, CustomEventType.EventRemoved.name(), customEvent.getEvent());

                            break;

                        case "ResetConditionResolver":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey() + " --> " + customEvent.getJobStream());

                            FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
                            filterConsumedInConditions.setJobSchedulerId(super.getSettings().getSchedulerId());
                            filterConsumedInConditions.setSession(customEvent.getSession());
                            filterConsumedInConditions.setJobStream(customEvent.getJobStream());
                            filterConsumedInConditions.setJob(customEvent.getJob());

                            conditionResolver.removeConsumedInconditions(sosHibernateSession, filterConsumedInConditions);

                            filterEvents = new FilterEvents();
                            filterEvents.setSchedulerId(getSettings().getSchedulerId());
                            filterEvents.setSession(customEvent.getSession());
                            filterEvents.setJob(customEvent.getJob());
                            filterEvents.setJobStream(customEvent.getJobStream());
                            conditionResolver.removeEventsFromJobStream(sosHibernateSession, filterEvents);

                            break;
                        }
                        break;
                    default:
                        LOGGER.debug(jobSchedulerEvent.getType() + " skipped");
                    }
                }
            }
            if (resolveInConditions) {
                resolveInConditions(sosHibernateSession);
            }
        } catch (

        Exception e) {
            getNotifier().smartNotifyOnError(getClass(), e);
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
            LOGGER.debug("closing reporting factory");
            reportingFactory.close();
            reportingFactory = null;
        }
    }

    private void createReportingFactory(Path configFile) throws Exception {
        LOGGER.debug("open reporting factory");
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

    public void setPeriodBegin(String periodBegin) {
        LOGGER.debug("Period starts at: " + periodBegin);
        Constants.periodBegin = periodBegin;
    }

}
