package com.sos.jobstreams.plugins;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.hibernate.classes.SOSHibernateFactory;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.exceptions.SOSHibernateOpenSessionException;
import com.sos.jitl.eventhandler.EventMeta.EventType;
import com.sos.jitl.eventhandler.handler.LoopEventHandler;
import com.sos.jitl.eventhandler.plugin.notifier.Notifier;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jitl.jobstreams.classes.JSEvent;
import com.sos.jitl.jobstreams.db.DBItemInCondition;
import com.sos.jitl.jobstreams.db.DBItemJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarter;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamStarters;
import com.sos.jitl.jobstreams.db.FilterConsumedInConditions;
import com.sos.jitl.jobstreams.db.FilterEvents;
import com.sos.jitl.jobstreams.db.FilterJobStreamHistory;
import com.sos.jitl.jobstreams.db.FilterJobStreamStarters;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jobstreams.classes.ConditionCustomEvent;
import com.sos.jobstreams.classes.DurationCalculator;
import com.sos.jobstreams.classes.JobSchedulerEvent;
import com.sos.jobstreams.classes.JobStarterOptions;
import com.sos.jobstreams.classes.OrderFinishedEvent;
import com.sos.jobstreams.classes.PublishEventOrder;
import com.sos.jobstreams.classes.QueuedEvents;
import com.sos.jobstreams.classes.ResolveOutConditionResult;
import com.sos.jobstreams.classes.StartJobReturn;
import com.sos.jobstreams.classes.TaskEndEvent;
import com.sos.jobstreams.resolver.JSConditionResolver;
import com.sos.jobstreams.resolver.JSHistoryEntry;
import com.sos.jobstreams.resolver.JSInCondition;
import com.sos.jobstreams.resolver.JSInConditionCommand;
import com.sos.jobstreams.resolver.JSJobStream;
import com.sos.jobstreams.resolver.JSJobStreamStarter;
import com.sos.joc.db.inventory.instances.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.scheduler.engine.eventbus.EventPublisher;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

import sos.util.SOSString;

public class JobSchedulerJobStreamsEventHandler extends LoopEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerJobStreamsEventHandler.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    public static final String CUSTOM_EVENT_KEY = JobSchedulerJobStreamsEventHandler.class.getSimpleName();;
    private SOSHibernateFactory reportingFactory;
    private QueuedEvents addQueuedEvents;
    private QueuedEvents delQueuedEvents;
    private Timer globalEventsPollTimer;
    private Timer nextJobStartTimer;
    private Timer publishEventTimer;

    private JSJobStreamStarter nextStarter;
    private int waitInterval = 0;
    private String session;
    private boolean synchronizeNextStart;
    JSConditionResolver conditionResolver;
    private Collection<PublishEventOrder> listOfPublishEventOrders;

    public static enum CustomEventType {
        InconditionValidated, EventCreated, EventRemoved, JobStreamRemoved, JobStreamStarted, StartTime, TaskEnded, InConditionConsumed, IsAlive, JobStreamCompleted
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

    public void resetPublishEventTimer() {
        if (publishEventTimer != null) {
            publishEventTimer.cancel();
            publishEventTimer.purge();
        }
        publishEventTimer = new Timer();
        publishEventTimer.schedule(new PublishEventTask(), 0, 1000);
    }

    public void resetGlobalEventsPollTimer() {
        if (globalEventsPollTimer != null) {
            globalEventsPollTimer.cancel();
            globalEventsPollTimer.purge();
        }
        globalEventsPollTimer = new Timer();
        globalEventsPollTimer.schedule(new GlobalPollingTask(), 60 * 1000, 60 * 1000);
        if (isDebugEnabled) {
            // LOGGER.debug("60s Polling for is activated");
        }
    }

    public void addPublishEventOrder(PublishEventOrder publishEventOrder) {
        boolean resetPublishEventTimer = false;
        if (listOfPublishEventOrders == null) {
            listOfPublishEventOrders = Collections.synchronizedCollection(new ArrayList<>());
        }
        if (listOfPublishEventOrders.isEmpty()) {
            resetPublishEventTimer = true;

        }
        listOfPublishEventOrders.add(publishEventOrder);
        if (resetPublishEventTimer) {
            resetPublishEventTimer();
        }
    }

    private void refreshStarters() throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, Exception {
        SOSHibernateSession sosHibernateSession = null;
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:execute");
            DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
            FilterJobStreamStarters filter = new FilterJobStreamStarters();

            List<DBItemJobStreamStarter> listOfStarter = dbLayerJobStreamStarters.getJobStreamStartersList(filter, 0);
            sosHibernateSession.beginTransaction();
            for (DBItemJobStreamStarter dbItemJobStreamStarter : listOfStarter) {
                Date now = new Date();
                if (dbItemJobStreamStarter.getRunTime() != null && !"".equals(dbItemJobStreamStarter.getRunTime())) {
                    if (dbItemJobStreamStarter.getNextStart() == null || dbItemJobStreamStarter.getNextStart().before(now)) {
                        Date nextStart = dbLayerJobStreamStarters.getNextStartTime(objectMapper, getSettings().getTimezone(), dbItemJobStreamStarter
                                .getRunTime());
                        if (nextStart != null) {
                            dbItemJobStreamStarter.setNextStart(nextStart);
                            dbLayerJobStreamStarters.update(dbItemJobStreamStarter);
                            LOGGER.debug("Refreshed next start for " + dbItemJobStreamStarter.getJobStream() + "." + dbItemJobStreamStarter.getTitle()
                                    + " to " + nextStart);
                        }
                    }
                }
            }
        } finally {
            sosHibernateSession.commit();
            sosHibernateSession.close();
        }

    }

    public void resetNextJobStartTimer(SOSHibernateSession sosHibernateSession) {

        while (synchronizeNextStart) {
            try {
                java.lang.Thread.sleep(1000);
                LOGGER.trace("Waiting for period synchronization...");
            } catch (InterruptedException e) {

            }
        }
        if (nextJobStartTimer != null) {
            nextJobStartTimer.cancel();
            nextJobStartTimer.purge();
        }
        nextJobStartTimer = new Timer();
        Long delay = -1L;

        do {
            if (!synchronizeNextStart && this.conditionResolver.getJsJobStreams() != null) {
                nextStarter = this.conditionResolver.getNextStarttime();

                if (nextStarter != null) {
                    DateTime nextDateTime = new DateTime(nextStarter.getNextStartFromList());
                    Long next = nextDateTime.getMillis();
                    Long now = new Date().getTime();
                    delay = next - now;
                    if (delay < 0) {
                        delay = 0L;
                    }
                    LOGGER.debug("Next start:" + nextStarter.getAllJobNames() + " at " + nextStarter.getNextStart());

                    try {
                        if (sosHibernateSession != null) {

                            DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
                            sosHibernateSession.beginTransaction();
                            DBItemJobStreamStarter dbItemJobStreamStarter = nextStarter.getItemJobStreamStarter();

                            dbItemJobStreamStarter.setNextStart(new Date(next));
                            dbLayerJobStreamStarters.update(dbItemJobStreamStarter);
                            sosHibernateSession.commit();
                            LOGGER.debug("Set next start for " + dbItemJobStreamStarter.getJobStream() + "." + dbItemJobStreamStarter.getTitle()
                                    + " to " + dbItemJobStreamStarter.getNextStart());

                            addPublishEventOrder(CUSTOM_EVENT_KEY, CustomEventType.StartTime.name(), String.valueOf(nextStarter
                                    .getItemJobStreamStarter().getId()));

                        }
                    } catch (SOSHibernateException e) {
                        LOGGER.error("Could not update next start time for starter: " + nextStarter.getItemJobStreamStarter().getId() + ":"
                                + nextStarter.getItemJobStreamStarter().getTitle(), e);
                    }

                    nextJobStartTimer.schedule(new JobStartTask(), delay);
                }

            }
        } while (delay < 0 && nextStarter != null);
        if (nextStarter == null) {
            LOGGER.info("no more start times found");
        }

    }

    private void addPublishEventOrder(String customEventKey, String name, String value) {
        PublishEventOrder publishEventOrder = new PublishEventOrder();
        publishEventOrder.setEventKey(customEventKey);
        Map<String, String> values = new HashMap<String, String>();
        values.put(name, value);
        publishEventOrder.setValues(values);
        addPublishEventOrder(publishEventOrder);
    }

    private void addPublishEventOrder(String customEventKey, Map<String, String> values) {
        PublishEventOrder publishEventOrder = new PublishEventOrder();
        publishEventOrder.setEventKey(customEventKey);
        publishEventOrder.setValues(values);
        addPublishEventOrder(publishEventOrder);
    }

    public class PublishEventTask extends TimerTask {

        public void run() {
            MDC.put("plugin", getIdentifier());
            boolean published = false;
            try {
                if (listOfPublishEventOrders != null) {
                    synchronized (listOfPublishEventOrders) {
                        for (PublishEventOrder publishEventOrder : listOfPublishEventOrders) {

                            if (!publishEventOrder.isPublished()) {

                                if (isDebugEnabled) {
                                    LOGGER.debug("Publish custom event:" + publishEventOrder.asString());
                                }
                                publishCustomEvent(publishEventOrder.getEventKey(), publishEventOrder.getValues());
                                publishEventOrder.setPublished(true);
                                published = true;
                            }
                        }
                    }
                    if (!published || listOfPublishEventOrders.size() > 1000) {
                        if (publishEventTimer != null) {
                            publishEventTimer.cancel();
                            publishEventTimer.purge();
                        }
                        listOfPublishEventOrders.clear();
                    }
                }

            } catch (

            Exception e) {
                e.printStackTrace();
                LOGGER.error("Timer Task Error in PublishEventTask", e);
            }

        }
    }

    public class GlobalPollingTask extends TimerTask {

        public void run() {
            MDC.put("plugin", getIdentifier());

            SOSHibernateSession sosHibernateSession;
            try {
                sosHibernateSession = reportingFactory.openStatelessSession();
                try {
                    if (conditionResolver.haveGlobalEvents()) {
                        // synchronizeNextStart = true;
                        // conditionResolver.reInitEvents(sosHibernateSession);
                        // synchronizeNextStart = false;
                    }
                    // refreshStarters();
                    // resolveInConditions(sosHibernateSession);
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
            MDC.put("plugin", getIdentifier());

            while (synchronizeNextStart) {
                try {
                    java.lang.Thread.sleep(1000);
                    LOGGER.trace("Waiting for period synchronization...");
                } catch (InterruptedException e) {

                }
            }
            SOSHibernateSession sosHibernateSession = null;
            try {
                sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:JobStartTask.run");
            } catch (SOSHibernateOpenSessionException e) {
                LOGGER.error("Could not initiate session", e);
            }

            try {
                LOGGER.debug("Start of jobs ==>" + nextStarter.getAllJobNames() + " at " + nextStarter.getNextStart());
                nextStarter.setLastStart(nextStarter.getNextStart().getTime());

                if (ACTIVE.equals(nextStarter.getItemJobStreamStarter().getState())) {
                    nextStarter.initActualParameters();
                    startJobs(nextStarter);
                } else {
                    LOGGER.debug("Starter: " + nextStarter.getItemJobStreamStarter().getId() + "." + nextStarter.getItemJobStreamStarter().getTitle()
                            + " is not active. Not started");
                }
                if (nextStarter.getNextStartFromList() == null) {
                    nextStarter.schedule();
                }
                DateTime nextDateTime = new DateTime(nextStarter.getNextStartFromList());

                Long now = nextStarter.getNextStart().getTime();
                Long next = nextDateTime.getMillis();

                Long delay = next - now;

                if (delay >= 0) {
                    LOGGER.debug("Recalculated next start:" + nextStarter.getAllJobNames() + " at " + new Date(next));

                    if (sosHibernateSession != null) {

                        DBLayerJobStreamStarters dbLayerJobStreamStarters = new DBLayerJobStreamStarters(sosHibernateSession);
                        sosHibernateSession.beginTransaction();
                        DBItemJobStreamStarter dbItemJobStreamStarter = nextStarter.getItemJobStreamStarter();

                        dbItemJobStreamStarter.setNextStart(new Date(next));
                        dbLayerJobStreamStarters.update(dbItemJobStreamStarter);
                        sosHibernateSession.commit();
                        LOGGER.debug("Set next start for " + dbItemJobStreamStarter.getJobStream() + "." + dbItemJobStreamStarter.getTitle() + " to "
                                + dbItemJobStreamStarter.getNextStart());
                        addPublishEventOrder(CUSTOM_EVENT_KEY, CustomEventType.StartTime.name(), String.valueOf(nextStarter.getItemJobStreamStarter()
                                .getId()));

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
        List<JobStarterOptions> listOfHandledJobs = jobStreamStarter.startJobs(getXmlCommandExecutor());

        UUID contextId = UUID.randomUUID();

        SOSHibernateSession sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:startJobs");
        sosHibernateSession.beginTransaction();
        try {

            DBItemJobStreamHistory dbItemJobStreamHistory = new DBItemJobStreamHistory();
            dbItemJobStreamHistory.setSchedulerId(super.getSettings().getSchedulerId());
            dbItemJobStreamHistory.setContextId(contextId.toString());
            dbItemJobStreamHistory.setCreated(new Date());
            dbItemJobStreamHistory.setJobStream(jobStreamStarter.getItemJobStreamStarter().getJobStream());
            dbItemJobStreamHistory.setJobStreamStarter(jobStreamStarter.getItemJobStreamStarter().getId());
            dbItemJobStreamHistory.setRunning(true);
            dbItemJobStreamHistory.setStarted(new Date());

            JSHistoryEntry historyEntry = new JSHistoryEntry();
            historyEntry.setCreated(new Date());
            historyEntry.setItemJobStreamHistory(dbItemJobStreamHistory);
            conditionResolver.addParameters(contextId, jobStreamStarter.getListOfActualParameters());

            JSJobStream jobStream = conditionResolver.getJsJobStreams().getJobStream(jobStreamStarter.getItemJobStreamStarter().getJobStream());
            LOGGER.debug(String.format("Adding history entry with context-id %s to jobStream %s", dbItemJobStreamHistory.getContextId(), jobStream
                    .getJobStream()));
            jobStream.getJsHistory().addHistoryEntry(historyEntry, sosHibernateSession);
            sosHibernateSession.commit();

            for (JobStarterOptions handledJob : listOfHandledJobs) {
                if (handledJob.isSkipped() && !handledJob.isSkipOutCondition()) {
                    TaskEndEvent taskEndEvent = new TaskEndEvent();
                    taskEndEvent.setJobPath(handledJob.getJob());
                    taskEndEvent.setReturnCode(0);
                    taskEndEvent.setTaskId("");
                    taskEndEvent.setEvaluatedContextId(contextId);
                    LOGGER.debug(String.format("Job %s skipped. Job run will be simulated with rc=0", handledJob.getJob()));
                    performTaskEnd(sosHibernateSession, taskEndEvent);

                } else {
                    sosHibernateSession.beginTransaction();
                    conditionResolver.getJobStreamContexts().addTaskToContext(contextId, super.getSettings().getSchedulerId(), handledJob,
                            sosHibernateSession);
                    sosHibernateSession.commit();
                }
            }
            Map<String, String> values = new HashMap<String, String>();
            values.put(CustomEventType.JobStreamStarted.name(), jobStream.getJobStream() + "." + jobStreamStarter.getItemJobStreamStarter()
                    .getTitle());
            values.put("contextId", contextId.toString());
            addPublishEventOrder(CUSTOM_EVENT_KEY, values);

            LOGGER.debug(jobStreamStarter.getAllJobNames() + " started");
            if (conditionResolver.getListOfHistoryIds() == null) {
                LOGGER.debug("!list of historyIds is null");
                synchronizeNextStart = true;
                conditionResolver.reInit(sosHibernateSession);
                conditionResolver.getJsJobStreams().reInitLastStart(nextStarter);
                synchronizeNextStart = false;
            }
            conditionResolver.getListOfHistoryIds().put(contextId, historyEntry.getItemJobStreamHistory());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
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

        synchronizeNextStart = false;
        super.onActivate(notifier);
        String method = "onActivate";

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
            session = Constants.getSession();
            Constants.baseUrl = this.getBaseUrl();

            conditionResolver = new JSConditionResolver(this.getXmlCommandExecutor(), this.getSettings());
            conditionResolver.setWorkingDirectory(System.getProperty("user.dir"));
            LOGGER.debug("onActivate init condition resolver");
            conditionResolver.init(sosHibernateSession);
            LOGGER.debug("onActivate reset timers");
            this.resetGlobalEventsPollTimer();
            this.resetNextJobStartTimer(sosHibernateSession);
            refreshStarters();
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

        LOGGER.debug("Events: " + SOSString.toString(events));
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
        ResolveOutConditionResult resolveOutConditionResult = conditionResolver.resolveOutConditions(sosHibernateSession, taskEndEvent, getSettings().getSchedulerId(), taskEndEvent
                .getJobPath());
        UUID contextId = conditionResolver.getJobStreamContexts().getContext(taskEndEvent.getTaskIdLong());
        if (contextId != null) {
            
            if (resolveOutConditionResult.isJobstreamCompleted()) {
                Map<String, String> values = new HashMap<String, String>();
                values.put(CustomEventType.JobStreamCompleted.name(), resolveOutConditionResult.getJobStream());
                values.put("contextId", contextId.toString());
                addPublishEventOrder(CUSTOM_EVENT_KEY, values);
            }
            conditionResolver.enableInconditionsForJob(getSettings().getSchedulerId(), taskEndEvent.getJobPath(), contextId);

            for (JSEvent jsNewEvent : conditionResolver.getNewJsEvents().getListOfEvents().values()) {
                if (jsNewEvent.getSession().equals(contextId.toString())) {
                    Map<String, String> values = new HashMap<String, String>();
                    values.put(CustomEventType.EventCreated.name(), jsNewEvent.getEvent());
                    values.put("contextId", contextId.toString());
                    addPublishEventOrder(CUSTOM_EVENT_KEY, values);
                }
            }
            for (JSEvent jsNewEvent : conditionResolver.getRemoveJsEvents().getListOfEvents().values()) {
                if (jsNewEvent.getSession().equals(contextId.toString())) {
                    Map<String, String> values = new HashMap<String, String>();
                    values.put(CustomEventType.EventRemoved.name(), jsNewEvent.getEvent());
                    values.put("contextId", contextId.toString());
                    addPublishEventOrder(CUSTOM_EVENT_KEY, values);
                }
            }
        }
        if (!conditionResolver.getNewJsEvents().isEmpty() || !conditionResolver.getRemoveJsEvents().isEmpty()) {
            boolean reinint = false;
            addQueuedEvents.handleEventlistBuffer(conditionResolver.getNewJsEvents());
            if (resolveOutConditionResult.isDbChange() && !this.addQueuedEvents.isEmpty()) {
                this.addQueuedEvents.storetoDb(sosHibernateSession, conditionResolver.getJsEvents());
                reinint = true;
            }
            delQueuedEvents.handleEventlistBuffer(conditionResolver.getRemoveJsEvents());
            if (resolveOutConditionResult.isDbChange() && !this.delQueuedEvents.isEmpty()) {
                this.addQueuedEvents.deleteFromDb(sosHibernateSession, conditionResolver.getJsEvents());
                reinint = true;
            }
            if (reinint) {
                synchronizeNextStart = true;
                conditionResolver.reInitConsumedInConditions(sosHibernateSession);
                synchronizeNextStart = false;
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

                List<JSInCondition> listOfValidatedInconditions = conditionResolver.resolveInConditions(sosHibernateSession);
                if (listOfValidatedInconditions != null) {
                    for (JSInCondition jsInCondition : listOfValidatedInconditions) {
                        LOGGER.debug("checking whether to execute out conditions for skipped jobs");
                        LOGGER.debug("isSkipOutCondition:" + jsInCondition.isSkipOutCondition());
                        Map<String, String> values = new HashMap<String, String>();
                        values.put(CustomEventType.InconditionValidated.name(), jsInCondition.getJob());
                        values.put("contextId", jsInCondition.getEvaluatedContextId().toString());
                        addPublishEventOrder(CUSTOM_EVENT_KEY, values);
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
                    synchronizeNextStart = true;
                    conditionResolver.reInit(sosHibernateSession);
                    conditionResolver.getJsJobStreams().reInitLastStart(nextStarter);
                    synchronizeNextStart = false;

                } catch (SOSHibernateException e) {
                    conditionResolver = null;
                    getNotifier().smartNotifyOnError(getClass(), e);
                    LOGGER.error(String.format("%s: %s", method, e.toString()), e);
                    throw new RuntimeException(e);
                }
            }

            Map<String, String> values;

            DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);
            FilterJobStreamHistory filterJobStreamHistory = new FilterJobStreamHistory();

            for (JsonValue entry : events) {
                if (entry != null) {
                    if (isDebugEnabled) {
                        LOGGER.debug(entry.toString());
                    }
                    JobSchedulerEvent jobSchedulerEvent = new JobSchedulerEvent((JsonObject) entry);
                    FilterEvents filterEvents = null;

                    switch (jobSchedulerEvent.getType()) {

                    case "OrderFinished":
                        LOGGER.debug("Event to be executed: " + jobSchedulerEvent.getType());
                        OrderFinishedEvent orderFinishedEvent = new OrderFinishedEvent((JsonObject) entry);
                        conditionResolver.checkHistoryCache(sosHibernateSession, orderFinishedEvent.getJobChain(), null);
                        conditionResolver.checkHistoryCache(sosHibernateSession, orderFinishedEvent.getJobChain() + "(" + orderFinishedEvent
                                .getOrderId() + ")", null);
                        break;
                    case "TaskEnded":
                        LOGGER.debug("Event to be executed: " + jobSchedulerEvent.getType());

                        TaskEndEvent taskEndEvent = new TaskEndEvent((JsonObject) entry);
                        LOGGER.debug("== >Task ended: " + taskEndEvent.getTaskId());
                        values = new HashMap<String, String>();
                        values.put(CustomEventType.TaskEnded.name(), taskEndEvent.getTaskId());
                        values.put("path", taskEndEvent.getJobPath());
                        if (conditionResolver.getJobStreamContexts().getContext(taskEndEvent.getTaskIdLong()) != null) {
                            values.put("contextId", conditionResolver.getJobStreamContexts().getContext(taskEndEvent.getTaskIdLong()).toString());
                        }

                        performTaskEnd(sosHibernateSession, taskEndEvent);

                        resolveInConditions = true;
                        addPublishEventOrder(CUSTOM_EVENT_KEY, values);
                        break;
                    case "TaskClosed":
                        break;

                    case "VariablesCustomEvent":
                        // {"variables":{"source":"CustomEventsUtilTest"},"TYPE":"VariablesCustomEvent","key":"InitConditionResolver","eventId":1554989954492000}
                        ConditionCustomEvent customEvent = new ConditionCustomEvent((JsonObject) entry);
                        switch (customEvent.getKey()) {

                        case "InitConditionResolver":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            synchronizeNextStart = true;
                            conditionResolver.reInit(sosHibernateSession);
                            conditionResolver.getJsJobStreams().reInitLastStart(nextStarter);
                            synchronizeNextStart = false;
                            this.resetGlobalEventsPollTimer();
                            resolveInConditions = true;
                            resetNextJobStartTimer(sosHibernateSession);
                            addPublishEventOrder(CUSTOM_EVENT_KEY, CustomEventType.StartTime.name(), "");
                            break;
                        case "StartJobStream":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            JSJobStreamStarter jsJobStreamStarter = conditionResolver.getListOfJobStreamStarter().get(customEvent
                                    .getJobStreamStarterId());
                            if (jsJobStreamStarter != null) {
                                jsJobStreamStarter.initActualParameters();
                                for (Entry<String, String> param : customEvent.getParameters().entrySet()) {
                                    jsJobStreamStarter.addActualParameter(param.getKey(), param.getValue());
                                }
                                startJobs(jsJobStreamStarter);
                                resolveInConditions = true;

                            } else {
                                LOGGER.warn("Could not find JobStream starter with id: " + customEvent.getJobStreamStarterId());
                            }
                            this.resetGlobalEventsPollTimer();

                            break;
                        case "StartJob":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            JSInConditionCommand jsInConditionCommand = new JSInConditionCommand();
                            jsInConditionCommand.setCommand("startjob");
                            jsInConditionCommand.setCommandParam(customEvent.getAt() + ", force=yes");

                            String session = customEvent.getSession();
                            UUID contextId = UUID.fromString(session);
                            LOGGER.debug("Start task for job " + customEvent.getJob() + " instance:" + session);
                            DBItemJobStreamHistory dbItemJobStreamHistory = null;
                            filterJobStreamHistory = new FilterJobStreamHistory();
                            filterJobStreamHistory.setSchedulerId(super.getSettings().getSchedulerId());
                            filterJobStreamHistory.addContextId(session);
                            dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);
                            List<DBItemJobStreamHistory> listOfJobStreamHistory = dbLayerJobStreamHistory.getJobStreamHistoryList(
                                    filterJobStreamHistory, 0);
                            if (listOfJobStreamHistory.size() > 0) {
                                dbItemJobStreamHistory = listOfJobStreamHistory.get(0);
                            }
                            if (dbItemJobStreamHistory != null) {
                                Long jobStreamId = dbItemJobStreamHistory.getJobStream();
                                JSJobStream jsJobStream = conditionResolver.getJsJobStreams().getJobStream(jobStreamId);
                                if (jsJobStream != null) {
                                    String jobStream = jsJobStream.getJobStream();
                                    JSInCondition inCondition = new JSInCondition();
                                    DBItemInCondition itemInCondition = new DBItemInCondition();
                                    itemInCondition.setJob(customEvent.getJob());
                                    itemInCondition.setJobStream(jobStream);
                                    inCondition.setItemInCondition(itemInCondition);
                                    JobStarterOptions jobStarterOptions = new JobStarterOptions();
                                    jobStarterOptions.setJob(inCondition.getNormalizedJob());

                                    if (conditionResolver.getListOfParameters().get(contextId) == null) {
                                        conditionResolver.addParameters(contextId, new HashMap<String, String>());
                                    }

                                    for (Entry<String, String> param : customEvent.getParameters().entrySet()) {
                                        conditionResolver.getListOfParameters().get(contextId).put(param.getKey(), param.getValue());
                                    }
                                    StartJobReturn startJobReturn = jsInConditionCommand.startJob(this.getXmlCommandExecutor(), inCondition,
                                            conditionResolver.getListOfParameters().get(contextId));
                                    sosHibernateSession.beginTransaction();
                                    conditionResolver.handleStartedJob(contextId, sosHibernateSession, startJobReturn, inCondition);
                                    sosHibernateSession.commit();

                                } else {
                                    LOGGER.warn("Could not start task. JobStream with id " + jobStreamId + " not found.");
                                }
                            } else {
                                LOGGER.warn("Could not start task. Instance " + session + " not found.");

                            }
                            break;
                        case "CalendarUsageUpdated":
                        case "CalendarDeleted":

                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            synchronizeNextStart = true;
                            conditionResolver.reInit(sosHibernateSession);
                            conditionResolver.reinitCalendarUsage(sosHibernateSession);
                            conditionResolver.getJsJobStreams().reInitLastStart(nextStarter);
                            synchronizeNextStart = false;

                            this.resetGlobalEventsPollTimer();
                            resetNextJobStartTimer(sosHibernateSession);
                            addPublishEventOrder(CUSTOM_EVENT_KEY, CustomEventType.StartTime.name(), "");

                            resolveInConditions = true;
                            break;
                        case "StartConditionResolver":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            synchronizeNextStart = true;
                            conditionResolver.reInitEvents(sosHibernateSession);
                            synchronizeNextStart = false;
                            resolveInConditions = true;
                            break;

                        case "IsAlive":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey() + " --> " + customEvent.getEvent());
                            addPublishEventOrder(CUSTOM_EVENT_KEY, CustomEventType.IsAlive.name(), super.getSettings().getSchedulerId());
                            break;
                        case "AddEvent":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey() + " --> " + customEvent.getEvent());

                            filterJobStreamHistory.addContextId(customEvent.getSession());
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

                                values = new HashMap<String, String>();
                                values.put(CustomEventType.EventCreated.name(), customEvent.getEvent());
                                values.put("contextId", customEvent.getSession());
                                String path = "";
                                if ((conditionResolver.getJsJobStreams() != null) && (conditionResolver.getJsJobStreams().getJobStreamByName(
                                        customEvent.getJobStream()) != null)) {
                                    path = Paths.get(conditionResolver.getJsJobStreams().getJobStreamByName(customEvent.getJobStream()).getFolder())
                                            .toString().replace('\\', '/');

                                }
                                values.put("path", path);

                                addPublishEventOrder(CUSTOM_EVENT_KEY, values);
                                addQueuedEvents.handleEventlistBuffer(conditionResolver.getNewJsEvents());

                                if (!conditionResolver.getNewJsEvents().isEmpty() && !this.addQueuedEvents.isEmpty()) {
                                    this.addQueuedEvents.storetoDb(sosHibernateSession, conditionResolver.getJsEvents());
                                    synchronizeNextStart = true;
                                    conditionResolver.reInitConsumedInConditions(sosHibernateSession);
                                    synchronizeNextStart = false;
                                }
                            }

                            break;
                        case "RemoveEvent":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey() + " --> " + customEvent.getEvent());
                            JSEvent event = new JSEvent();
                            event.setCreated(new Date());
                            event.setEvent(customEvent.getEvent());
                            event.setJobStream(customEvent.getJobStream());

                            event.setSession(customEvent.getSession());
                            event.setSchedulerId(super.getSettings().getSchedulerId());
                            event.setGlobalEvent(customEvent.isGlobalEvent());

                            conditionResolver.removeEvent(sosHibernateSession, event);

                            event.setSession(Constants.getSession());
                            conditionResolver.removeEvent(sosHibernateSession, event);

                            delQueuedEvents.handleEventlistBuffer(conditionResolver.getRemoveJsEvents());
                            if (!conditionResolver.getRemoveJsEvents().isEmpty() && !this.delQueuedEvents.isEmpty()) {
                                this.addQueuedEvents.deleteFromDb(sosHibernateSession, conditionResolver.getJsEvents());
                                synchronizeNextStart = true;
                                conditionResolver.reInitConsumedInConditions(sosHibernateSession);
                                synchronizeNextStart = false;
                            }

                            values = new HashMap<String, String>();
                            String path = "";
                            if ((conditionResolver.getJsJobStreams() != null) && (conditionResolver.getJsJobStreams().getJobStreamByName(customEvent
                                    .getJobStream()) != null)) {
                                path = Paths.get(conditionResolver.getJsJobStreams().getJobStreamByName(customEvent.getJobStream()).getFolder())
                                        .toString().replace('\\', '/');

                            }
                            values.put("path", path);
                            values.put(CustomEventType.EventRemoved.name(), customEvent.getEvent());
                            values.put("contextId", customEvent.getSession());

                            addPublishEventOrder(CUSTOM_EVENT_KEY, values);

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
