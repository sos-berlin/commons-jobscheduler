package com.sos.jobstreams.resolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;
import com.sos.jitl.eventing.evaluate.BooleanExp;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jitl.jobstreams.classes.JSEvent;
import com.sos.jitl.jobstreams.classes.JSEventKey;
import com.sos.jitl.jobstreams.db.DBItemConsumedInCondition;
import com.sos.jitl.jobstreams.db.DBItemInCondition;
import com.sos.jitl.jobstreams.db.DBItemInConditionWithCommand;
import com.sos.jitl.jobstreams.db.DBItemJobStream;
import com.sos.jitl.jobstreams.db.DBItemJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBItemJobStreamTaskContext;
import com.sos.jitl.jobstreams.db.DBItemOutConditionWithConfiguredEvent;
import com.sos.jitl.jobstreams.db.DBItemOutConditionWithEvent;
import com.sos.jitl.jobstreams.db.DBLayerConsumedInConditions;
import com.sos.jitl.jobstreams.db.DBLayerEvents;
import com.sos.jitl.jobstreams.db.DBLayerInConditions;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamHistory;
import com.sos.jitl.jobstreams.db.DBLayerJobStreams;
import com.sos.jitl.jobstreams.db.DBLayerJobStreamsTaskContext;
import com.sos.jitl.jobstreams.db.DBLayerOutConditions;
import com.sos.jitl.jobstreams.db.FilterConsumedInConditions;
import com.sos.jitl.jobstreams.db.FilterEvents;
import com.sos.jitl.jobstreams.db.FilterInConditions;
import com.sos.jitl.jobstreams.db.FilterJobStreamHistory;
import com.sos.jitl.jobstreams.db.FilterJobStreamTaskContext;
import com.sos.jitl.jobstreams.db.FilterJobStreams;
import com.sos.jitl.jobstreams.db.FilterOutConditions;
import com.sos.jobstreams.classes.CheckHistoryCacheRule;
import com.sos.jobstreams.classes.CheckHistoryCacheRules;
import com.sos.jobstreams.classes.CheckHistoryCondition;
import com.sos.jobstreams.classes.CheckHistoryKey;
import com.sos.jobstreams.classes.CheckHistoryValue;
import com.sos.jobstreams.classes.DurationCalculator;
import com.sos.jobstreams.classes.EventDate;
import com.sos.jobstreams.classes.JobStarterOptions;
import com.sos.jobstreams.classes.StartJobReturn;
import com.sos.jobstreams.classes.TaskEndEvent;
import com.sos.jobstreams.resolver.interfaces.IJSCondition;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

import sos.util.SOSString;

public class JSConditionResolver {

    private static final String JOB = "job";
    private static final String JOB_CHAIN = "jobchain";
    private static final Logger LOGGER = LoggerFactory.getLogger(JSConditionResolver.class);
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private JSJobInConditions jsJobInConditions;
    private JSJobStreamInConditions jsJobStreamInConditions;
    private JSJobOutConditions jsJobOutConditions;
    private JSJobStreamOutConditions jsJobStreamOutConditions;
    private JSEvents jsEvents;
    private BooleanExp booleanExpression;
    private EventHandlerSettings settings;
    private CheckHistoryCondition checkHistoryCondition;
    private CheckHistoryCacheRules listOfCheckHistoryChacheRules;
    private SchedulerXmlCommandExecutor schedulerXmlCommandExecutor;
    private String workingDirectory = "";
    private JSEvents newJsEvents = new JSEvents();
    private JSEvents removeJsEvents = new JSEvents();
    private JSJobStreams jsJobStreams;
    private JobStreamContexts jobStreamContexts;
    private Map<UUID, DBItemJobStreamHistory> listOfHistoryIds;
    private Map<UUID, Map<String, String>> listOfParameters;
    private Map<Long, JSJobStreamStarter> listOfJobStreamStarter;

    public JSConditionResolver(SchedulerXmlCommandExecutor schedulerXmlCommandExecutor, EventHandlerSettings settings) {
        super();
        this.schedulerXmlCommandExecutor = schedulerXmlCommandExecutor;
        booleanExpression = new BooleanExp("");
        this.settings = settings;
        checkHistoryCondition = new CheckHistoryCondition(settings.getSchedulerId());
    }

    public JSConditionResolver(String schedulerId) {
        super();
        booleanExpression = new BooleanExp("");
        this.settings = new EventHandlerSettings();
        settings.setSchedulerId(schedulerId);
        checkHistoryCondition = new CheckHistoryCondition(settings.getSchedulerId());
    }

    public void reInit(SOSHibernateSession sosHibernateSession) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException,
            Exception {
        LOGGER.debug("JSConditionResolver reinit jobstream model");
        jsJobInConditions = null;
        jsJobStreamInConditions = null;
        jsJobOutConditions = null;
        jsJobStreamOutConditions = null;
        jsJobStreams = null;
        jsEvents = null;
        listOfHistoryIds = null;
        listOfParameters = null;
        listOfJobStreamStarter = null;
        checkHistoryCondition = new CheckHistoryCondition(settings.getSchedulerId());

        this.init(sosHibernateSession);

    }

    public void reInitEvents(SOSHibernateSession session) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver reinit events injobstream model");
        try {
            jsEvents = null;
            this.initEvents(session);
        } catch (Exception e) {
            LOGGER.error("Could not reInit Events", e);

        }
    }

    public void reinitCalendarUsage(SOSHibernateSession sosHibernateSession) {
        for (JSInConditions jsInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
            for (JSInCondition jsInCondition : jsInConditions.getListOfInConditions().values()) {
                jsInCondition.setListOfDates(sosHibernateSession, settings.getSchedulerId());
            }
        }
        for (JSInConditions jsInConditions : jsJobStreamInConditions.getListOfJobStreamInConditions().values()) {
            for (JSInCondition jsInCondition : jsInConditions.getListOfInConditions().values()) {
                jsInCondition.setListOfDates(sosHibernateSession, settings.getSchedulerId());
            }
        }
    }

    public boolean haveGlobalEvents() {
        return this.jsJobInConditions.getHaveGlobalConditions();
    }

    public void init(SOSHibernateSession sosHibernateSession) throws JsonParseException, JsonMappingException, JsonProcessingException, IOException,
            Exception {

        LOGGER.debug("JSConditionResolver::Init");
        DurationCalculator duration = new DurationCalculator();
        listOfParameters = new HashMap<UUID, Map<String, String>>();
        listOfJobStreamStarter = new HashMap<Long, JSJobStreamStarter>();

        if (jobStreamContexts == null) {
            jobStreamContexts = new JobStreamContexts();
            FilterJobStreamHistory filterJobStreamHistory = new FilterJobStreamHistory();
            filterJobStreamHistory.setRunning(true);
            DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);
            DBLayerJobStreamsTaskContext dbLayerJobStreamsTaskContext = new DBLayerJobStreamsTaskContext(sosHibernateSession);
            List<DBItemJobStreamHistory> listOfRunningStreams = dbLayerJobStreamHistory.getJobStreamHistoryList(filterJobStreamHistory, 0);
            for (DBItemJobStreamHistory dbItemJobStreamHistory : listOfRunningStreams) {
                FilterJobStreamTaskContext filterTaskContext = new FilterJobStreamTaskContext();
                filterTaskContext.setJobstreamHistoryId(dbItemJobStreamHistory.getContextId());
                List<DBItemJobStreamTaskContext> listOfTaskContext = dbLayerJobStreamsTaskContext.getJobStreamStarterJobsList(filterTaskContext, 0);
                jobStreamContexts.setTaskToContext(listOfTaskContext);
            }
        }
        if (jsJobInConditions == null) {

            FilterJobStreamHistory filterJobStreamHistory = new FilterJobStreamHistory();
            DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);
            filterJobStreamHistory.setRunning(true);
            List<DBItemJobStreamHistory> listOfRunningJobStreams = dbLayerJobStreamHistory.getJobStreamHistoryList(filterJobStreamHistory, 0);

            HashMap<Long, List<DBItemConsumedInCondition>> mapOfConsumedInCondition = new HashMap<Long, List<DBItemConsumedInCondition>>();
            for (DBItemJobStreamHistory dbItemJobStreamHistory : listOfRunningJobStreams) {

                FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
                filterConsumedInConditions.setJobSchedulerId(settings.getSchedulerId());
                filterConsumedInConditions.setSession(dbItemJobStreamHistory.getContextId());
                DBLayerConsumedInConditions dbLayerCoumsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);
                List<DBItemConsumedInCondition> l = dbLayerCoumsumedInConditions.getConsumedInConditionsList(filterConsumedInConditions, 0);
                for (DBItemConsumedInCondition dbItemConsumedInCondition : l) {
                    if (mapOfConsumedInCondition.get(dbItemConsumedInCondition.getInConditionId()) == null) {
                        mapOfConsumedInCondition.put(dbItemConsumedInCondition.getInConditionId(), new ArrayList<DBItemConsumedInCondition>());
                    }
                    mapOfConsumedInCondition.get(dbItemConsumedInCondition.getInConditionId()).add(dbItemConsumedInCondition);
                }
            }

            FilterInConditions filterInConditions = new FilterInConditions();
            filterInConditions.setJobSchedulerId(settings.getSchedulerId());

            DBLayerInConditions dbLayerInConditions = new DBLayerInConditions(sosHibernateSession);
            List<DBItemInConditionWithCommand> listOfInConditions = dbLayerInConditions.getInConditionsList(filterInConditions, 0);
            for (DBItemInConditionWithCommand dbItemInCondition : listOfInConditions) {
                DBItemInCondition inInCondition = dbItemInCondition.getDbItemInCondition();
                if (inInCondition != null && mapOfConsumedInCondition.get(inInCondition.getId()) != null) {
                    for (DBItemConsumedInCondition dbItemConsumedInCondition : mapOfConsumedInCondition.get(inInCondition.getId())) {
                        dbItemInCondition.setConsumed(dbItemConsumedInCondition.getSession());
                    }
                }
            }

            jsJobInConditions = new JSJobInConditions(settings);
            jsJobInConditions.setListOfJobInConditions(sosHibernateSession, listOfInConditions);

            jsJobStreamInConditions = new JSJobStreamInConditions(settings);
            jsJobStreamInConditions.setListOfJobInConditions(jsJobInConditions);

        }
        if (jsJobOutConditions == null) {
            FilterOutConditions filterOutConditions = new FilterOutConditions();
            filterOutConditions.setJobSchedulerId(settings.getSchedulerId());
            DBLayerOutConditions dbLayerOutConditions = new DBLayerOutConditions(sosHibernateSession);
            List<DBItemOutConditionWithConfiguredEvent> listOfOutConditions = dbLayerOutConditions.getOutConditionsList(filterOutConditions, 0);
            jsJobOutConditions = new JSJobOutConditions();
            jsJobOutConditions.setListOfJobOutConditions(listOfOutConditions);
            jsJobStreamOutConditions = new JSJobStreamOutConditions();
            jsJobStreamOutConditions.setListOfJobStreamOutConditions(jsJobOutConditions);
        }

        if (jsJobStreams == null) {
            FilterJobStreams filterJobStreams = new FilterJobStreams();
            filterJobStreams.setSchedulerId(settings.getSchedulerId());
            DBLayerJobStreams dbLayerJobStreams = new DBLayerJobStreams(sosHibernateSession);
            List<DBItemJobStream> listOfJobStreams = dbLayerJobStreams.getJobStreamsList(filterJobStreams, 0);
            jsJobStreams = new JSJobStreams();
            jsJobStreams.setListOfJobStreams(settings, listOfJobStreams, listOfJobStreamStarter, sosHibernateSession);
        }

        if (listOfHistoryIds == null) {
            listOfHistoryIds = new HashMap<UUID, DBItemJobStreamHistory>();
            FilterJobStreamHistory filterJobStreamHistory = new FilterJobStreamHistory();
            filterJobStreamHistory.setSchedulerId(settings.getSchedulerId());
            DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);
            List<DBItemJobStreamHistory> listOfJobStreamHistory = dbLayerJobStreamHistory.getJobStreamHistoryList(filterJobStreamHistory, 0);
            for (DBItemJobStreamHistory dbItemJobStreamHistory : listOfJobStreamHistory) {
                try {
                    this.listOfHistoryIds.put(UUID.fromString(dbItemJobStreamHistory.getContextId()), dbItemJobStreamHistory);
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Could not read create uuid from: " + dbItemJobStreamHistory.getContextId());
                }
            }
        }

        if (listOfCheckHistoryChacheRules == null) {
            listOfCheckHistoryChacheRules = new CheckHistoryCacheRules();
            listOfCheckHistoryChacheRules.initCacheRules();
        }

        initEvents(sosHibernateSession);
        initCheckHistory(sosHibernateSession);
        if (isDebugEnabled || true) {
            duration.end("Init JobStreams condition model ");
            LOGGER.debug("In Conditions: " + jsJobInConditions.getListOfJobInConditions().size());
            LOGGER.debug("Out Conditions: " + jsJobOutConditions.getListOfJobOutConditions().size());
            LOGGER.debug("Events: " + jsEvents.getListOfEvents().size());
        }

    }

    public void initEvents(SOSHibernateSession session) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver::initEvents");
        if (jsEvents == null) {
            jsEvents = new JSEvents();
            FilterEvents filterEvents = new FilterEvents();
            filterEvents.setSchedulerId(settings.getSchedulerId());
            filterEvents.setIncludingGlobalEvent(true);
            DBLayerEvents dbLayerEvents = new DBLayerEvents(session);
            List<DBItemOutConditionWithEvent> listOfEvents = dbLayerEvents.getEventsList(filterEvents, 0);
            jsEvents.setListOfEvents(listOfEvents);
        }
    }

    public void reInitConsumedInConditions(SOSHibernateSession sosHibernateSession) throws SOSHibernateException {
        for (Map.Entry<Long, JSJobStream> jobStream : jsJobStreams.getListOfJobStreams().entrySet()) {
            LOGGER.debug("reInitConsumedInConditions for jobstream" + jobStream.getValue().getJobStream());
            if (jobStream.getValue().getListOfJobStreamHistory() != null) {
                for (JSHistoryEntry jsHistoryEntry : jobStream.getValue().getListOfJobStreamHistory()) {
                    UUID contextId = jsHistoryEntry.getContextId();

                    LOGGER.debug("JSConditionResolver::reInitConsumedInConditions");
                    FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
                    filterConsumedInConditions.setJobSchedulerId(settings.getSchedulerId());
                    filterConsumedInConditions.setSession(contextId.toString());
                    DBLayerConsumedInConditions dbLayerCoumsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);

                    List<DBItemConsumedInCondition> listOfConsumedInConditions = dbLayerCoumsumedInConditions.getConsumedInConditionsList(
                            filterConsumedInConditions, 0);
                    HashMap<Long, DBItemConsumedInCondition> mapOfConsumedInCondition = new HashMap<Long, DBItemConsumedInCondition>();
                    for (DBItemConsumedInCondition dbItemConsumedInCondition : listOfConsumedInConditions) {
                        mapOfConsumedInCondition.put(dbItemConsumedInCondition.getInConditionId(), dbItemConsumedInCondition);
                    }

                    sosHibernateSession.beginTransaction();
                    for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
                        for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                            if (inCondition.isConsumed(contextId) && mapOfConsumedInCondition.get(inCondition.getId()) == null) {
                                DBItemConsumedInCondition dbItemConsumedInCondition = new DBItemConsumedInCondition();
                                dbItemConsumedInCondition.setCreated(new Date());
                                dbItemConsumedInCondition.setInConditionId(inCondition.getId());
                                dbItemConsumedInCondition.setSession(contextId.toString());
                                dbLayerCoumsumedInConditions.store(dbItemConsumedInCondition);
                                if (isTraceEnabled) {
                                    LOGGER.trace(String.format("Consumed In Condition stored %s", inCondition.toStr()));
                                }

                            }
                            if (!inCondition.isConsumed(contextId) && mapOfConsumedInCondition.get(inCondition.getId()) != null) {
                                filterConsumedInConditions.setInConditionId(inCondition.getId());
                                dbLayerCoumsumedInConditions.deleteByInConditionId(filterConsumedInConditions);
                                if (isTraceEnabled) {
                                    LOGGER.trace(String.format("Consumed In Condition deleted %s", inCondition.toStr()));
                                }
                            }
                        }

                    }
                }
            }
        }
        sosHibernateSession.commit();
    }

    public void initCheckHistory(SOSHibernateSession sosHibernateSession) throws SOSHibernateException {

        LOGGER.debug("JSConditionResolver::initCheckHistory");
        for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
            for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                String expressionValue = inCondition.getExpression();
                List<JSCondition> listOfConditions = JSConditions.getListOfConditions(expressionValue);
                for (JSCondition jsCondition : listOfConditions) {
                    try {
                        switch (jsCondition.getConditionType()) {
                        case JOB: {
                            checkHistoryCondition.validateJob(sosHibernateSession, jsCondition, inCondition.getJob(), 0);
                            break;
                        }
                        case JOB_CHAIN: {
                            checkHistoryCondition.validateJobChain(sosHibernateSession, jsCondition);
                            break;
                        }
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Could not validate expression:" + expressionValue + " in In Condition of job " + inCondition.getJob());
                    }
                }
            }
        }

        for (JSOutConditions jobOutConditions : jsJobOutConditions.getListOfJobOutConditions().values()) {
            for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                String expressionValue = outCondition.getExpression() + " ";
                List<JSCondition> listOfConditions = JSConditions.getListOfConditions(expressionValue);
                for (JSCondition jsCondition : listOfConditions) {
                    try {
                        switch (jsCondition.getConditionType().toLowerCase()) {
                        case JOB: {
                            checkHistoryCondition.validateJob(sosHibernateSession, jsCondition, outCondition.getJob(), 0);
                            break;
                        }
                        case JOB_CHAIN: {
                            checkHistoryCondition.validateJobChain(sosHibernateSession, jsCondition);
                            break;
                        }
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Could not validate expression:" + expressionValue + " in In Condition of job " + outCondition.getJob());
                    }
                }
            }
        }
    }

    private String expressionPrepare(String e) {
        return "<###" + e.replaceAll("\\(", "###(###").replaceAll("\\)", "###)###").replaceAll(" and ", "###&&&###").replaceAll(" or ", "###|||###")
                .replaceAll("not ", "###!!!###") + "###>";

    }

    private String expressionBack(String e) {
        return e.replaceAll("\\#\\#\\#\\(\\#\\#\\#", "(").replaceAll("\\#\\##\\)\\#\\#\\#", ")").replaceAll("\\#\\#\\#\\&\\&\\&\\#\\#\\#", " and ")
                .replaceAll("\\#\\#\\#\\|\\|\\|\\#\\#\\#", " or ").replaceAll("\\#\\#\\#\\!\\!\\!\\#\\#\\#", "not ").replaceAll("<\\#\\#\\#", "")
                .replaceAll("\\#\\#\\#>", "");

    }

    public boolean validate(SOSHibernateSession sosHibernateSession, Integer taskReturnCode, UUID contextId, IJSCondition condition) {
        String expressionValue = condition.getExpression() + " ";
        expressionValue = JSConditions.normalizeExpression(expressionValue);
        List<JSCondition> listOfConditions = JSConditions.getListOfConditions(expressionValue);
        LOGGER.trace("JSConditionResolver::validate");
        for (JSCondition jsCondition : listOfConditions) {
            LOGGER.trace("JSConditionResolver::validate --> " + jsCondition.getConditionType());

            switch (jsCondition.getConditionType()) {
            case "rc":
            case "returncode": {
                JSReturnCodeResolver returnCodeResolver = new JSReturnCodeResolver();
                if (returnCodeResolver.resolve(taskReturnCode, jsCondition.getConditionParam())) {
                    expressionValue = this.expressionPrepare(expressionValue);
                    expressionValue = expressionValue.replace("###" + jsCondition.getConditionValue() + "###", "###true###");
                    expressionValue = this.expressionBack(expressionValue);
                }

                break;
            }
            case "fileexist": {
                String fileName = jsCondition.getConditionParam().replace('"', ' ').trim();
                fileName = fileName.replaceAll("%20", " ");
                Path p = Paths.get(fileName);
                File f = null;
                if (p.isAbsolute()) {
                    f = new File(fileName);
                } else {
                    f = new File(workingDirectory, fileName);
                }
                try {
                    LOGGER.debug("check file: " + f.getCanonicalPath());
                } catch (IOException e) {
                    LOGGER.warn("Can not debug the path of the file.");
                }
                if (f.exists()) {
                    LOGGER.debug("file " + jsCondition.getConditionParam() + " exists");
                    expressionValue = this.expressionPrepare(expressionValue);
                    expressionValue = expressionValue.replace("###" + jsCondition.getConditionValue() + "###", "###true###");
                    expressionValue = this.expressionBack(expressionValue);
                } else {
                    LOGGER.debug("file " + jsCondition.getConditionParam() + " does not exist");
                }

                break;
            }
            case JOB: {
                try {
                    if (checkHistoryCondition.validateJob(sosHibernateSession, jsCondition, condition.getJob(), taskReturnCode).getValidateResult()) {
                        expressionValue = this.expressionPrepare(expressionValue);
                        expressionValue = expressionValue.replace("###" + jsCondition.getConditionValue() + "###", "###true###");
                        expressionValue = this.expressionBack(expressionValue);

                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                break;
            }
            case JOB_CHAIN: {
                try {
                    if (checkHistoryCondition.validateJobChain(sosHibernateSession, jsCondition).getValidateResult()) {
                        expressionValue = this.expressionPrepare(expressionValue);
                        expressionValue = expressionValue.replace("###" + jsCondition.getConditionValue() + "###", "###true###");
                        expressionValue = this.expressionBack(expressionValue);

                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                break;
            }
            case "global":
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
                if (jsCondition.typeIsGlobalEvent()) {
                    String session = eventDate.getEventDate(date);
                    LOGGER.debug("Global: setting context to: " + session);
                    jsEventKey.setSession(session);
                } else {
                    String session = contextId.toString();
                    LOGGER.debug("NOT Global: setting session to: " + session);
                    jsEventKey.setSession(session);
                }

                jsEventKey.setGlobalEvent(jsCondition.typeIsGlobalEvent());
                jsEventKey.setSchedulerId(settings.getSchedulerId());
                if (jsCondition.getConditionJobStream().isEmpty()) {
                    jsEventKey.setJobStream(condition.getJobStream());
                } else {
                    jsEventKey.setJobStream(jsCondition.getConditionJobStream());
                }

                if (jsEventKey.getJobStream() != null && !condition.getJobStream().equals(jsEventKey.getJobStream())) {
                    String session = eventDate.getEventDate(date);
                    LOGGER.debug("Jobstream context: setting session to: " + session);
                    jsEventKey.setSession(session);
                }

                JSEvent jsEvent = jsEvents.getEventByJobStream(jsEventKey);
                if (jsEvent != null) {
                    expressionValue = this.expressionPrepare(expressionValue);
                    expressionValue = expressionValue.replace("###" + jsCondition.getConditionValue() + "###", "###true###");
                    expressionValue = this.expressionBack(expressionValue);
                }

                break;
            }
            default:
                LOGGER.warn("unknown conditionType: " + jsCondition.getConditionType());
            }

        }
        LOGGER.trace(condition.getExpression() + " after replacement  -->  " + expressionValue);
        booleanExpression.setBoolExp(expressionValue);
        boolean evaluatedExpression = booleanExpression.evaluateExpression();
        LOGGER.trace(condition.getExpression() + " evaluated to: " + evaluatedExpression);
        return evaluatedExpression;
    }

    public void handleStartedJob(UUID contextId, SOSHibernateSession sosHibernateSession, StartJobReturn startJobReturn, JSInCondition inCondition)
            throws SOSHibernateException {
        if (!startJobReturn.getStartedJob().isEmpty()) {
            JobStarterOptions jobStarterOptions = new JobStarterOptions();
            jobStarterOptions.setJob(startJobReturn.getStartedJob());
            jobStarterOptions.setJobStream(inCondition.getJobStream());
            jobStarterOptions.setTaskId(startJobReturn.getTaskId());
            jobStreamContexts.addTaskToContext(contextId, settings.getSchedulerId(), jobStarterOptions, sosHibernateSession);
            this.disableInconditionsForJob(settings.getSchedulerId(), startJobReturn.getStartedJob(), contextId);
        }
    }

    public List<JSInCondition> resolveInConditions(SOSHibernateSession sosHibernateSession) throws NumberFormatException, Exception {

        LOGGER.debug("JSConditionResolver::resolveInConditions");

        List<JSInCondition> listOfValidatedInconditions = new ArrayList<JSInCondition>();

        for (Map.Entry<Long, JSJobStream> jobStream : jsJobStreams.getListOfJobStreams().entrySet()) {
            LOGGER.debug("Resolving jobstream " + jobStream.getValue().getJobStream());
            if (jobStream.getValue() != null && jobStream.getValue().getListOfJobStreamHistory() != null) {
                if (jobStream.getValue() != null) {
                    for (JSHistoryEntry jsHistoryEntry : jobStream.getValue().getListOfJobStreamHistory()) {
                        if (jsHistoryEntry.isRunning()) {
                            LOGGER.debug(String.format("Running JobStream: %s mit contextId %s found", jsHistoryEntry.getItemJobStreamHistory()
                                    .getJobStream(), jsHistoryEntry.getContextId()));
                            UUID contextId = jsHistoryEntry.getContextId();
                            if (jsJobInConditions != null && jsJobInConditions.getListOfJobInConditions().size() == 0) {
                                LOGGER.debug("No in conditions defined. Nothing to do");
                            } else {
                                sosHibernateSession.beginTransaction();
                                try {
                                    for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
                                        for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                                            String logPrompt = "";
                                            if (isTraceEnabled) {
                                                logPrompt = "job: " + inCondition.getJob() + " Job Stream: " + inCondition.getJobStream()
                                                        + " Expression: " + inCondition.getExpression();
                                            }
                                            if (!(inCondition.isConsumed(contextId) && inCondition.isMarkExpression())) {
                                                if (!inCondition.jobIsRunning(contextId)) {

                                                    if (isTraceEnabled) {
                                                        LOGGER.trace("---InCondition is: " + inCondition.toStr());
                                                    }

                                                    JSJobStream jsJobStream = this.getJsJobStreams().getJobStream(jsHistoryEntry
                                                            .getItemJobStreamHistory().getJobStream());
                                                    JobStreamContexts jobStreamContexts = this.getJobStreamContexts();
                                                    boolean endedByJob = jsHistoryEntry.endOfStream(jsJobStream, jobStreamContexts);
                                                    if (!endedByJob) {
                                                        if (validate(sosHibernateSession, null, contextId, inCondition)) {
                                                            StartJobReturn startJobReturn = inCondition.executeCommand(sosHibernateSession, contextId,
                                                                    listOfParameters.get(contextId), schedulerXmlCommandExecutor);
                                                            handleStartedJob(contextId, sosHibernateSession, startJobReturn, inCondition);
                                                            inCondition.setEvaluatedContextId(contextId);
                                                            LOGGER.debug("Adding in condition: " + SOSString.toString(inCondition));
                                                            listOfValidatedInconditions.add(inCondition);
                                                        } else {
                                                            if (isTraceEnabled) {
                                                                LOGGER.trace(logPrompt + " evaluated to --> false");
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    if (isTraceEnabled) {
                                                        LOGGER.trace(logPrompt + " not executed --> job is running");
                                                    }
                                                }
                                            } else {
                                                if (isTraceEnabled) {
                                                    LOGGER.trace(logPrompt + " not executed --> already consumed");
                                                }
                                            }
                                        }
                                    }
                                    sosHibernateSession.commit();
                                } catch (Exception e) {
                                    LOGGER.error(e.getMessage(), e);
                                    sosHibernateSession.rollback();
                                }
                            }
                        }
                    }
                }
            }
        }
        return listOfValidatedInconditions;

    }

    public void checkRunning(SOSHibernateSession sosHibernateSession, UUID contextId) throws SOSHibernateException {
        for (Map.Entry<Long, JSJobStream> jobStream : jsJobStreams.getListOfJobStreams().entrySet()) {
            if (jobStream.getValue() != null && jobStream.getValue().getListOfJobStreamHistory() != null) {
                List<JSHistoryEntry> toRemove = new ArrayList<JSHistoryEntry>();
                for (JSHistoryEntry jsHistoryEntry : jobStream.getValue().getListOfJobStreamHistory()) {
                    if (jsHistoryEntry.getContextId().toString().equals(contextId.toString()) && jsHistoryEntry.isRunning()) {
                        LOGGER.debug(String.format("Running JobStream: %s mit contextId %s found", jsHistoryEntry.getItemJobStreamHistory()
                                .getJobStream(), jsHistoryEntry.getContextId()));
                        if (!jsHistoryEntry.checkRunning(this)) {
                            try {
                                LOGGER.debug(jsHistoryEntry.getContextId() + " --> running=false");
                                jsHistoryEntry.getItemJobStreamHistory().setRunning(false);
                                DBLayerJobStreamHistory dbLayerJobStreamHistory = new DBLayerJobStreamHistory(sosHibernateSession);
                                DBItemJobStreamHistory dbItemJobStreamHistory = dbLayerJobStreamHistory.getJobStreamHistoryDbItem(jsHistoryEntry
                                        .getId());
                                if (dbItemJobStreamHistory != null) {
                                    sosHibernateSession.beginTransaction();
                                    dbItemJobStreamHistory.setRunning(false);
                                    dbLayerJobStreamHistory.update(dbItemJobStreamHistory);
                                    sosHibernateSession.commit();
                                    toRemove.add(jsHistoryEntry);
                                    this.listOfParameters.remove(contextId);
                                }
                                jobStream.getValue().getListOfJobStreamHistory().removeAll(toRemove);
                            } catch (Exception e) {
                                LOGGER.error(e.getMessage(), e);
                                sosHibernateSession.rollback();
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean resolveOutConditions(SOSHibernateSession sosHibernateSession, TaskEndEvent taskEndEvent, String jobSchedulerId, String job)
            throws SOSHibernateException {

        LOGGER.debug("JSConditionResolver::resolveOutConditions for job:" + job);
        String defaultSession = Constants.getSession();
        boolean dbChange = false;

        JSJobConditionKey jobConditionKey = new JSJobConditionKey();
        this.newJsEvents = new JSEvents();
        this.removeJsEvents = new JSEvents();
        jobConditionKey.setJob(job);
        jobConditionKey.setJobSchedulerId(jobSchedulerId);
        JSOutConditions jobOutConditions = jsJobOutConditions.getListOfJobOutConditions().get(jobConditionKey);
        if (jobOutConditions != null && jobOutConditions.getListOfOutConditions().size() == 0) {
            LOGGER.debug("No out conditions defined. Nothing to do");
        } else {
            UUID contextId = this.getJobStreamContexts().getContext(taskEndEvent.getTaskIdLong());
            if (contextId == null) {
                contextId = taskEndEvent.getEvaluatedContextId();
                LOGGER.debug("using contextId from in condition");
            }
            if (contextId != null) {
                checkRunning(sosHibernateSession, contextId);
                LOGGER.debug("resolve outconditions using context:" + contextId.toString());
                if (jobOutConditions != null) {
                    for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                        String expression = outCondition.getJob() + ":" + outCondition.getExpression();

                        LOGGER.trace("---OutCondition: " + expression);
                        if (validate(sosHibernateSession, taskEndEvent.getReturnCode(), contextId, outCondition)) {
                            LOGGER.trace("create/remove events ------>");
                            DBItemJobStreamHistory historyEntry = listOfHistoryIds.get(contextId);
                            dbChange = outCondition.storeOutConditionEvents(sosHibernateSession, contextId.toString(), historyEntry, jsEvents,
                                    newJsEvents, removeJsEvents);
                            outCondition.storeOutConditionEvents(sosHibernateSession, defaultSession, historyEntry, jsEvents, newJsEvents,
                                    removeJsEvents);
                        } else {
                            LOGGER.trace(expression + "-->false");
                        }
                        LOGGER.trace("------------------------------------");
                    }
                } else {
                    LOGGER.debug("No out conditions for job " + job + " found. Nothing to do");
                }
                if (this.listOfHistoryIds.get(contextId) != null && !this.listOfHistoryIds.get(contextId).getRunning()) {
                    this.listOfHistoryIds.remove(contextId);
                }
            }
        }

        return dbChange;
    }

    public void setJobIsRunningInconditionsForJob(boolean value, String jobSchedulerId, String job, UUID contextId) throws SOSHibernateException {
        LOGGER.debug("setJobIsRunningInconditionsForJob: " + contextId);
        if (contextId != null) {
            JSJobConditionKey jobConditionKey = new JSJobConditionKey();
            LOGGER.debug("job:" + job);
            LOGGER.debug("jobSchedulerId:" + jobSchedulerId);
            jobConditionKey.setJob(job);
            jobConditionKey.setJobSchedulerId(jobSchedulerId);
            JSInConditions jobInConditions = jsJobInConditions.getListOfJobInConditions().get(jobConditionKey);
            if (jobInConditions != null && jobInConditions.getListOfInConditions().size() == 0) {
                LOGGER.debug("No in conditions defined. Nothing to do");
            } else {
                if (jobInConditions != null) {
                    for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                        inCondition.setJobIsRunning(contextId, value);
                        if (isTraceEnabled) {
                            if (value) {
                                LOGGER.trace("In conditions for job " + job + " disabled");
                            } else {
                                LOGGER.trace("In conditions for job " + job + " enabled");
                            }
                        }
                    }
                } else {
                    LOGGER.debug("No in conditions for job " + job + " found. Nothing to do");
                }
            }
        } else {
            LOGGER.debug("ContextId is null: " + job + " Nothing to do");
        }
    }

    public void enableInconditionsForJob(String jobSchedulerId, String job, UUID contextId) throws SOSHibernateException {
        setJobIsRunningInconditionsForJob(false, jobSchedulerId, job, contextId);
    }

    public void disableInconditionsForJob(String jobSchedulerId, String job, UUID contextId) throws SOSHibernateException {
        setJobIsRunningInconditionsForJob(true, jobSchedulerId, job, contextId);
    }

    public BooleanExp getBooleanExpression() {
        return booleanExpression;
    }

    public void removeConsumedInconditions(SOSHibernateSession sosHibernateSession, FilterConsumedInConditions filterConsumedInConditions)
            throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver::removeConsumedInconditions --> " + filterConsumedInConditions.getSession() + "::"
                + filterConsumedInConditions.getJobStream() + "." + filterConsumedInConditions.getJob());

        try {
            UUID contextId = UUID.fromString(filterConsumedInConditions.getSession());

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
                                    .isEmpty()) && inCondition.isConsumed(contextId)) {
                        LOGGER.trace(expression + " no longer consumed");
                        inCondition.removeConsumed(contextId);
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            LOGGER.warn("could not create contextId from " + filterConsumedInConditions.getSession());
        }

    }

    public void removeEventsFromJobStream(SOSHibernateSession sosHibernateSession, FilterEvents filter) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver::removeEventsFromJobStream --> " + filter.getJobStream() + "." + filter.getJob());
        try {
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.deleteEventsWithOutConditions(filter);
            sosHibernateSession.commit();

            jsEvents = null;
            initEvents(sosHibernateSession);
        } catch (

        Exception e) {
            LOGGER.error(e.getMessage(), e);
            sosHibernateSession.rollback();
        }
    }

    public void addEvent(SOSHibernateSession sosHibernateSession, JSEvent event) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver::addEvent --> " + event.getJobStream() + "." + event.getEvent());
        this.newJsEvents = new JSEvents();
        event.store(sosHibernateSession);
        jsEvents.addEvent(event);
        newJsEvents.addEvent(event);
        LOGGER.debug(event.getEvent() + " added");
    }

    public void removeEvent(SOSHibernateSession sosHibernateSession, JSEvent event) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver::removeEvent --> " + event.getEvent());
        this.removeJsEvents = new JSEvents();
        event.deleteEvent(sosHibernateSession);
        removeJsEvents.addEvent(event);
        jsEvents.removeEvent(event);
        LOGGER.debug(event.getEvent() + " removed");
    }

    public Boolean eventExists(JSEventKey jsEventKey) {
        JSEvent jsEvent = jsEvents.getEventByJobStream(jsEventKey);
        return jsEvent != null;
    }

    public void checkHistoryCache(SOSHibernateSession sosHibernateSession, String jobPath, Integer taskReturnCode) throws Exception {
        CheckHistoryKey checkHistoryKey = new CheckHistoryKey(JOB, jobPath, "");
        if (listOfCheckHistoryChacheRules == null) {
            LOGGER.warn("History not initialized");
        } else {
            for (CheckHistoryCacheRule checkHistoryCacheRule : listOfCheckHistoryChacheRules.getListOfCheckHistoryChacheRules()) {
                checkHistoryKey.setQuery(checkHistoryCacheRule.getQueryString());
                CheckHistoryValue validateResult = checkHistoryCondition.getCache(checkHistoryKey);
                if (validateResult != null && ((checkHistoryCacheRule.isValidateAlways()) || (checkHistoryCacheRule.isValidateIfFalse()
                        && !validateResult.getValidateResult()))) {
                    checkHistoryCondition.putCache(checkHistoryKey, null);
                    checkHistoryCondition.validateJob(sosHibernateSession, validateResult.getJsCondition(), jobPath, taskReturnCode);
                }
            }
        }
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;

    }

    public JSEvents getNewJsEvents() {
        return newJsEvents;
    }

    public JSEvents getJsEvents() {
        return jsEvents;
    }

    public JSEvents getRemoveJsEvents() {
        return removeJsEvents;
    }

    public JSJobStreamStarter getNextStarttime() {
        return jsJobStreams.getNextStarter();
    }

    public JobStreamContexts getJobStreamContexts() {
        return jobStreamContexts;
    }

    public JSJobStreams getJsJobStreams() {
        return jsJobStreams;
    }

    public JSJobStreamInConditions getJsJobStreamInConditions() {
        return jsJobStreamInConditions;
    }

    public JSJobStreamOutConditions getJsJobStreamOutConditions() {
        return jsJobStreamOutConditions;
    }

    public void addParameters(UUID uuid, Map<String, String> listOfParameters) {
        this.listOfParameters.put(uuid, listOfParameters);
    }

    public Map<Long, JSJobStreamStarter> getListOfJobStreamStarter() {
        return listOfJobStreamStarter;
    }

    public Map<UUID, DBItemJobStreamHistory> getListOfHistoryIds() {
        return listOfHistoryIds;
    }

    public Map<UUID, Map<String, String>> getListOfParameters() {
        return listOfParameters;
    }

    public void setListOfParameters(Map<UUID, Map<String, String>> listOfParameters) {
        this.listOfParameters = listOfParameters;
    }

}
