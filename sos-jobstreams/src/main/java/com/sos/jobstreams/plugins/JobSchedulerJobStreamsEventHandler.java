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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

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
import com.sos.jitl.jobstreams.db.FilterJobStreamHistory;
import com.sos.jitl.jobstreams.db.FilterJobStreamStarters;
import com.sos.jitl.reporting.db.DBItemInventoryInstance;
import com.sos.jitl.reporting.db.DBLayer;
import com.sos.jobstreams.classes.CheckRunningResult;
import com.sos.jobstreams.classes.ConditionCustomEvent;
import com.sos.jobstreams.classes.DurationCalculator;
import com.sos.jobstreams.classes.JobSchedulerEvent;
import com.sos.jobstreams.classes.JobStarterOptions;
import com.sos.jobstreams.classes.OrderFinishedEvent;
import com.sos.jobstreams.classes.PublishEventOrder;
import com.sos.jobstreams.classes.QueuedEvents;
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
    private Timer nextJobStartTimer;
    private Timer publishEventTimer;
    private Timer checkEventTimer;

    private JSJobStreamStarter nextStarter;

    private String session;

    private Integer conditionResolverIndex;
    List<JSConditionResolver> listOfConditionResolver;
    private Collection<PublishEventOrder> listOfPublishEventOrders;
    private boolean initEvents;
    private CompletableFuture<Void> createNewModel = null;
    private static Semaphore synchronizeNextStart = new Semaphore(1, true);
    private static Semaphore eventHandlerSemaphore = new Semaphore(1, true);

    public static enum CustomEventType {
        InconditionValidated, EventCreated, EventRemoved, JobStreamRemoved, JobStreamStarted, StartTime, TaskEnded, InConditionConsumed, IsAlive, JobStreamCompleted
    }

    public static enum CustomEventTypeValue {
        incondition

    }

    public void resetPublishEventTimer() {
        if (publishEventTimer != null) {
            publishEventTimer.cancel();
            publishEventTimer.purge();
        }
        publishEventTimer = new Timer();
        publishEventTimer.schedule(new PublishEventTask(), 0, 1000);
    }

    public void resetCheckInitTimer() {
        if (checkEventTimer != null) {
            checkEventTimer.cancel();
            checkEventTimer.purge();
        }
        checkEventTimer = new Timer();
        checkEventTimer.schedule(new CheckEventTask(), 1000, 1000);
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

        try {
            synchronizeNextStart.acquire();
        } catch (InterruptedException e1) {
            LOGGER.info("resetNextJobStartTimer interrupted");

        }
        if (nextJobStartTimer != null) {
            nextJobStartTimer.cancel();
            nextJobStartTimer.purge();
        }
        nextJobStartTimer = new Timer();
        Long delay = -1L;

        try {
            do {
                if (this.getConditionResolver().getJsJobStreams() != null) {
                    nextStarter = this.getConditionResolver().getNextStarttime();

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
        } finally {
            synchronizeNextStart.release();
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

    private JSConditionResolver reInitModel() {
        LOGGER.debug("start initialization of jobstream model." + Constants.testDelay);
        JSConditionResolver j = new JSConditionResolver(getXmlCommandExecutor(), getSettings());
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = reportingFactory.openStatelessSession("JobstreamModelCreatorThread");
            j.reInitPartial(sosHibernateSession);
            if (Constants.testDelay != null) {
                LOGGER.debug("testDelay is null.");
                java.lang.Thread.sleep(10000);
            } else {
                java.lang.Thread.sleep(Constants.testDelay * 1000);
            }
            LOGGER.debug("new model is now available");
            eventHandlerSemaphore.acquire();
            assignNewModel(j);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause instanceof InterruptedException) {
                LOGGER.info("Init closed");
            } else {
                LOGGER.error(e.getMessage(), e);
            }
            j = null;
        } finally {
            eventHandlerSemaphore.release();
            if (sosHibernateSession != null) {
                sosHibernateSession.close();
            }
        }
        return j;
    }

    public class CheckEventTask extends TimerTask {

        public void run() {
            MDC.put("plugin", getIdentifier());

            if (initEvents) {
                initEvents = false;

                if ((createNewModel != null) && !createNewModel.isDone()) {
                    createNewModel.cancel(true);
                }
                LOGGER.debug("starting createNewModel...");
                createNewModel = CompletableFuture.supplyAsync(() -> {
                    MDC.put("plugin", getIdentifier());
                    return reInitModel();
                }).thenAccept(jsConditionResolver -> {
                    LOGGER.debug("New model creation is ready...");
                    /*
                     * try { assignNewModel2(jsConditionResolver); } catch (SOSHibernateException e) { LOGGER.error(e.getMessage(), e); }
                     */
                });

                checkEventTimer.cancel();
                checkEventTimer.purge();
            }
        }
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

    public class JobStartTask extends TimerTask {

        private static final String ACTIVE = "active";

        public void run() {
            MDC.put("plugin", getIdentifier());
            try {
                synchronizeNextStart.acquire();
            } catch (InterruptedException e1) {
                LOGGER.info("JobStartTask interrupted");
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
                synchronizeNextStart.release();
                if (sosHibernateSession != null) {
                    sosHibernateSession.close();
                }
            }
        }

    }

    private void reInitComplete() throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, Exception {
        try {
            synchronizeNextStart.acquire();
        } catch (InterruptedException e) {
            LOGGER.info("reInitComplete interrupted");
        }
        SOSHibernateSession sosHibernateSession;
        try {
            sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:reinitComplet");

            getConditionResolver().reInitComplete(sosHibernateSession);
            getConditionResolver().getJsJobStreams().reInitLastStart(nextStarter);
        } finally {
            synchronizeNextStart.release();
        }
    }

    private void reConsumedInConditions() throws JsonParseException, JsonMappingException, JsonProcessingException, IOException, Exception {
        try {
            synchronizeNextStart.acquire();
        } catch (InterruptedException e) {
            LOGGER.info("reConsumedInConditions interrupted");
        }
        SOSHibernateSession sosHibernateSession;
        try {
            sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:reinitComplet");
            getConditionResolver().reInitConsumedInConditions(sosHibernateSession);
        } finally {
            synchronizeNextStart.release();
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
            getConditionResolver().addParameters(contextId, jobStreamStarter.getListOfActualParameters());

            JSJobStream jobStream = getConditionResolver().getJsJobStreams().getJobStream(jobStreamStarter.getItemJobStreamStarter().getJobStream());
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
                    getConditionResolver().getJobStreamContexts().addTaskToContext(contextId, super.getSettings().getSchedulerId(), handledJob,
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
            if (getConditionResolver().getListOfHistoryIds() == null) {
                LOGGER.debug("!list of historyIds is null");
                reInitComplete();
            }
            getConditionResolver().getListOfHistoryIds().put(contextId, historyEntry.getItemJobStreamHistory());
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

    private void createConditionResolver() {
        LOGGER.debug("Create new jobstream model");

        if (this.listOfConditionResolver == null) {
            this.listOfConditionResolver = new ArrayList<JSConditionResolver>();
            this.conditionResolverIndex = 1;
        }
        if (conditionResolverIndex == 0) {
            conditionResolverIndex = 1;
        } else {
            if (conditionResolverIndex == 1) {
                conditionResolverIndex = 0;
            }
        }

        JSConditionResolver j = new JSConditionResolver(this.getXmlCommandExecutor(), this.getSettings());

        if (conditionResolverIndex >= this.listOfConditionResolver.size()) {
            LOGGER.debug("add new jobstream model");
            this.listOfConditionResolver.add(j);
        } else {
            LOGGER.debug("set new jobstream model " + conditionResolverIndex);
            this.listOfConditionResolver.set(conditionResolverIndex, j);
        }
    }

    private JSConditionResolver getLastConditionResolver() {
        LOGGER.debug("Get last Condition Resolver");
        int index = 0;
        if (conditionResolverIndex == 0) {
            index = 1;
        } else {
            if (conditionResolverIndex == 1) {
                index = 0;
            }
        }
        LOGGER.debug("====> " + index);
        return this.listOfConditionResolver.get(index);
    }

    private void copyConditionResolverData(SOSHibernateSession sosHibernateSession) throws SOSHibernateException {
        this.getConditionResolver().assign(sosHibernateSession, getLastConditionResolver());
        try {
            this.resolveInConditions(sosHibernateSession);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void addConditionResolver(JSConditionResolver j) {
        if (this.listOfConditionResolver == null) {
            this.listOfConditionResolver = new ArrayList<JSConditionResolver>();
            this.conditionResolverIndex = 1;
        }
        if (conditionResolverIndex == 0) {
            conditionResolverIndex = 1;
        } else {
            if (conditionResolverIndex == 1) {
                conditionResolverIndex = 0;
            }
        }

        if (conditionResolverIndex >= this.listOfConditionResolver.size()) {
            this.listOfConditionResolver.add(j);
        } else {
            this.listOfConditionResolver.set(conditionResolverIndex, j);
        }
    }

    private JSConditionResolver getConditionResolver() {
        LOGGER.debug("getConditionresolver with index " + conditionResolverIndex);
        return this.listOfConditionResolver.get(conditionResolverIndex);
    }

    @Override
    public void onActivate(Notifier notifier) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        LOGGER.debug("onActivate Plugin");
        LOGGER.debug("WorkingDirectory:" + System.getProperty("user.dir"));
        LOGGER.debug("TimeZone: " + TimeZone.getDefault().getID());

        initEvents = false;

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

            createConditionResolver();

            getConditionResolver().setWorkingDirectory(System.getProperty("user.dir"));
            LOGGER.debug("onActivate init condition resolver");
            getConditionResolver().initComplete(sosHibernateSession);
            LOGGER.debug("onActivate reset timers");
            this.resetNextJobStartTimer(sosHibernateSession);
            refreshStarters();
            LOGGER.debug("onActivate initEventHandler");

            EventType[] observedEventTypes = new EventType[] { EventType.TaskClosed, EventType.TaskEnded, EventType.VariablesCustomEvent,
                    EventType.OrderFinished };
            LOGGER.debug("Session: " + this.session);

            resolveInConditions(sosHibernateSession);
            sosHibernateSession.close();

            start(observedEventTypes);
        } catch (Exception e) {
            this.listOfConditionResolver = null;
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
        try {
            execute(true, eventId, events);
        } catch (InterruptedException e) {
            LOGGER.info("Interrupted Thread in jobstream plugin");
        }
    }

    @Override
    public void onProcessingEnd(Long eventId) {
        LOGGER.debug("Shutdown plugin");
        closeReportingFactory();
        LOGGER.debug("Plugin closed");
    }

    private void performTaskEnd(SOSHibernateSession sosHibernateSession, TaskEndEvent taskEndEvent) throws Exception {
        LOGGER.debug("TaskEnded event to be executed:" + taskEndEvent.getTaskId() + " " + taskEndEvent.getJobPath());
        getConditionResolver().checkHistoryCache(sosHibernateSession, taskEndEvent.getJobPath(), taskEndEvent.getReturnCode());
        boolean dbChanged = getConditionResolver().resolveOutConditions(sosHibernateSession, taskEndEvent, getSettings().getSchedulerId(),
                taskEndEvent.getJobPath());
        UUID contextId = getConditionResolver().getJobStreamContexts().getContext(taskEndEvent.getTaskIdLong());
        if (contextId != null) {

            getConditionResolver().enableInconditionsForJob(getSettings().getSchedulerId(), taskEndEvent.getJobPath(), contextId);
            CheckRunningResult checkRunningResult = getConditionResolver().checkRunning(sosHibernateSession, contextId);

            if (checkRunningResult.isJobstreamCompleted()) {
                Map<String, String> values = new HashMap<String, String>();
                values.put(CustomEventType.JobStreamCompleted.name(), checkRunningResult.getJobStream());
                values.put("contextId", contextId.toString());
                addPublishEventOrder(CUSTOM_EVENT_KEY, values);
            }

            for (JSEvent jsNewEvent : getConditionResolver().getNewJsEvents().getListOfEvents().values()) {
                if (jsNewEvent.getSession().equals(contextId.toString())) {
                    Map<String, String> values = new HashMap<String, String>();
                    values.put(CustomEventType.EventCreated.name(), jsNewEvent.getEvent());
                    values.put("contextId", contextId.toString());
                    addPublishEventOrder(CUSTOM_EVENT_KEY, values);
                }
            }
            for (JSEvent jsNewEvent : getConditionResolver().getRemoveJsEvents().getListOfEvents().values()) {
                if (jsNewEvent.getSession().equals(contextId.toString())) {
                    Map<String, String> values = new HashMap<String, String>();
                    values.put(CustomEventType.EventRemoved.name(), jsNewEvent.getEvent());
                    values.put("contextId", contextId.toString());
                    addPublishEventOrder(CUSTOM_EVENT_KEY, values);
                }
            }
        }
        if (!getConditionResolver().getNewJsEvents().isEmpty() || !getConditionResolver().getRemoveJsEvents().isEmpty()) {
            boolean reinint = false;
            addQueuedEvents.handleEventlistBuffer(getConditionResolver().getNewJsEvents());
            if (dbChanged && !this.addQueuedEvents.isEmpty()) {
                this.addQueuedEvents.storetoDb(sosHibernateSession, getConditionResolver().getJsEvents());
                reinint = true;
            }
            delQueuedEvents.handleEventlistBuffer(getConditionResolver().getRemoveJsEvents());
            if (dbChanged && !this.delQueuedEvents.isEmpty()) {
                this.addQueuedEvents.deleteFromDb(sosHibernateSession, getConditionResolver().getJsEvents());
                reinint = true;
            }
            if (reinint) {
                reConsumedInConditions();
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

                List<JSInCondition> listOfValidatedInconditions = getConditionResolver().resolveInConditions(sosHibernateSession);
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

    private void assignNewModel(JSConditionResolver jsConditionResolver) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:assignNewModel");
            LOGGER.debug("New JobStream model will be assigned now.");
            synchronizeNextStart.acquire();
            this.addConditionResolver(jsConditionResolver);
            this.copyConditionResolverData(sosHibernateSession);
            getConditionResolver().getJsJobStreams().reInitLastStart(nextStarter);
            resetNextJobStartTimer(sosHibernateSession);
            addPublishEventOrder(CUSTOM_EVENT_KEY, CustomEventType.StartTime.name(), "");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            sosHibernateSession.close();
            synchronizeNextStart.release();
            ;
        }
    }

    private void execute(boolean onNonEmptyEvent, Long eventId, JsonArray events) throws InterruptedException {
        String method = "execute";
        if (isDebugEnabled) {
            LOGGER.debug(String.format("%s: onNonEmptyEvent=%s, eventId=%s", method, onNonEmptyEvent, eventId));
        }
        boolean resolveInConditions = false;
        eventHandlerSemaphore.acquire();

        SOSHibernateSession sosHibernateSession = null;

        try {

            sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:execute");

            if (this.listOfConditionResolver == null || getConditionResolver() == null) {
                sosHibernateSession = reportingFactory.openStatelessSession("eventhandler:execute");
                createConditionResolver();
                try {
                    getConditionResolver().initComplete(sosHibernateSession);
                    this.resetNextJobStartTimer(sosHibernateSession);

                } catch (Exception e) {
                    this.listOfConditionResolver = null;
                    throw e;
                }
            }

            if (!Constants.getSession().equals(this.session)) {
                try {
                    this.session = Constants.getSession();
                    LOGGER.debug("Change session to: " + this.session);
                    reInitComplete();

                } catch (SOSHibernateException e) {
                    this.listOfConditionResolver = null;
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

                    switch (jobSchedulerEvent.getType()) {

                    case "OrderFinished":
                        LOGGER.debug("Event to be executed: " + jobSchedulerEvent.getType());
                        OrderFinishedEvent orderFinishedEvent = new OrderFinishedEvent((JsonObject) entry);
                        getConditionResolver().checkHistoryCache(sosHibernateSession, orderFinishedEvent.getJobChain(), null);
                        getConditionResolver().checkHistoryCache(sosHibernateSession, orderFinishedEvent.getJobChain() + "(" + orderFinishedEvent
                                .getOrderId() + ")", null);
                        break;
                    case "TaskEnded":
                        LOGGER.debug("Event to be executed: " + jobSchedulerEvent.getType());

                        TaskEndEvent taskEndEvent = new TaskEndEvent((JsonObject) entry);
                        LOGGER.debug("== >Task ended: " + taskEndEvent.getTaskId());
                        values = new HashMap<String, String>();
                        values.put(CustomEventType.TaskEnded.name(), taskEndEvent.getTaskId());
                        values.put("path", taskEndEvent.getJobPath());
                        if (getConditionResolver().getJobStreamContexts().getContext(taskEndEvent.getTaskIdLong()) != null) {
                            values.put("contextId", getConditionResolver().getJobStreamContexts().getContext(taskEndEvent.getTaskIdLong())
                                    .toString());
                        }

                        performTaskEnd(sosHibernateSession, taskEndEvent);

                        resolveInConditions = true;
                        addPublishEventOrder(CUSTOM_EVENT_KEY, values);
                        break;
                    case "TaskClosed":
                        break;

                    case "VariablesCustomEvent":
                        // {"variables":{"source":"CustomEventsUtilTest"},"TYPE":"VariablesCustomEvent","key":"InitgetConditionResolver()","eventId":1554989954492000}
                        ConditionCustomEvent customEvent = new ConditionCustomEvent((JsonObject) entry);
                        switch (customEvent.getKey()) {

                        case "InitConditionResolver":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            initEvents = true;
                            resetCheckInitTimer();
                            // jobstreamModelCreator.createResolver(reportingFactory);
                            break;
                        case "StartJobStream":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            JSJobStreamStarter jsJobStreamStarter = getConditionResolver().getListOfJobStreamStarter().get(customEvent
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
                                JSJobStream jsJobStream = getConditionResolver().getJsJobStreams().getJobStream(jobStreamId);
                                if (jsJobStream != null) {
                                    String jobStream = jsJobStream.getJobStream();
                                    JSInCondition inCondition = new JSInCondition();
                                    DBItemInCondition itemInCondition = new DBItemInCondition();
                                    itemInCondition.setJob(customEvent.getJob());
                                    itemInCondition.setJobStream(jobStream);
                                    inCondition.setItemInCondition(itemInCondition);
                                    JobStarterOptions jobStarterOptions = new JobStarterOptions();
                                    jobStarterOptions.setJob(inCondition.getNormalizedJob());

                                    if (getConditionResolver().getListOfParameters().get(contextId) == null) {
                                        getConditionResolver().addParameters(contextId, new HashMap<String, String>());
                                    }

                                    for (Entry<String, String> param : customEvent.getParameters().entrySet()) {
                                        getConditionResolver().getListOfParameters().get(contextId).put(param.getKey(), param.getValue());
                                    }
                                    StartJobReturn startJobReturn = jsInConditionCommand.startJob(this.getXmlCommandExecutor(), inCondition,
                                            getConditionResolver().getListOfParameters().get(contextId));
                                    sosHibernateSession.beginTransaction();
                                    getConditionResolver().handleStartedJob(contextId, sosHibernateSession, startJobReturn, inCondition);
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
                            // jobstreamModelCreator.createResolver(reportingFactory);
                            initEvents = true;
                            resetCheckInitTimer();
                            resolveInConditions = true;
                            break;
                        case "StartConditionResolver":
                            LOGGER.debug("VariablesCustomEvent event to be executed: " + customEvent.getKey());
                            synchronizeNextStart.acquire();
                            getConditionResolver().reInitEvents(sosHibernateSession);
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

                                getConditionResolver().addEvent(sosHibernateSession, event);
                                if (!customEvent.isGlobalEvent()) {
                                    event.setSession(Constants.getSession());
                                    getConditionResolver().addEvent(sosHibernateSession, event);
                                }

                                values = new HashMap<String, String>();
                                values.put(CustomEventType.EventCreated.name(), customEvent.getEvent());
                                values.put("contextId", customEvent.getSession());
                                String path = "";
                                if ((getConditionResolver().getJsJobStreams() != null) && (getConditionResolver().getJsJobStreams()
                                        .getJobStreamByName(customEvent.getJobStream()) != null)) {
                                    path = Paths.get(getConditionResolver().getJsJobStreams().getJobStreamByName(customEvent.getJobStream())
                                            .getFolder()).toString().replace('\\', '/');

                                }
                                values.put("path", path);

                                addPublishEventOrder(CUSTOM_EVENT_KEY, values);
                                addQueuedEvents.handleEventlistBuffer(getConditionResolver().getNewJsEvents());

                                if (!getConditionResolver().getNewJsEvents().isEmpty() && !this.addQueuedEvents.isEmpty()) {
                                    this.addQueuedEvents.storetoDb(sosHibernateSession, getConditionResolver().getJsEvents());
                                    reConsumedInConditions();
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

                            getConditionResolver().removeEvent(sosHibernateSession, event);

                            event.setSession(Constants.getSession());
                            getConditionResolver().removeEvent(sosHibernateSession, event);

                            delQueuedEvents.handleEventlistBuffer(getConditionResolver().getRemoveJsEvents());
                            if (!getConditionResolver().getRemoveJsEvents().isEmpty() && !this.delQueuedEvents.isEmpty()) {
                                this.addQueuedEvents.deleteFromDb(sosHibernateSession, getConditionResolver().getJsEvents());
                                reConsumedInConditions();
                            }

                            values = new HashMap<String, String>();
                            String path = "";
                            if ((getConditionResolver().getJsJobStreams() != null) && (getConditionResolver().getJsJobStreams().getJobStreamByName(
                                    customEvent.getJobStream()) != null)) {
                                path = Paths.get(getConditionResolver().getJsJobStreams().getJobStreamByName(customEvent.getJobStream()).getFolder())
                                        .toString().replace('\\', '/');

                            }
                            values.put("path", path);
                            values.put(CustomEventType.EventRemoved.name(), customEvent.getEvent());
                            values.put("contextId", customEvent.getSession());

                            addPublishEventOrder(CUSTOM_EVENT_KEY, values);

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
            synchronizeNextStart.release();
            eventHandlerSemaphore.release();
            if (sosHibernateSession != null) {
                sosHibernateSession.close();
            }
        }
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

    public void setTestDelay(String testDelay) {
        LOGGER.debug("Test delay " + testDelay);
        Constants.testDelay = 0;
        try {
            Constants.testDelay = Integer.parseInt(testDelay);
        } catch (NumberFormatException e) {
            Constants.testDelay = 0;
        }
    }

}
