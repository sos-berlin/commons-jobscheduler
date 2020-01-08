package com.sos.jobstreams.resolver;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernateFactory;
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
import com.sos.jitl.jobstreams.db.DBItemOutConditionWithConfiguredEvent;
import com.sos.jitl.jobstreams.db.DBItemOutConditionWithEvent;
import com.sos.jitl.jobstreams.db.DBLayerConsumedInConditions;
import com.sos.jitl.jobstreams.db.DBLayerEvents;
import com.sos.jitl.jobstreams.db.DBLayerInConditions;
import com.sos.jitl.jobstreams.db.DBLayerOutConditions;
import com.sos.jitl.jobstreams.db.FilterConsumedInConditions;
import com.sos.jitl.jobstreams.db.FilterEvents;
import com.sos.jitl.jobstreams.db.FilterInConditions;
import com.sos.jitl.jobstreams.db.FilterOutConditions;
import com.sos.jobstreams.classes.CheckHistoryCacheRule;
import com.sos.jobstreams.classes.CheckHistoryCacheRules;
import com.sos.jobstreams.classes.CheckHistoryCondition;
import com.sos.jobstreams.classes.CheckHistoryKey;
import com.sos.jobstreams.classes.CheckHistoryValue;
import com.sos.jobstreams.classes.DurationCalculator;
import com.sos.jobstreams.classes.EventDate;
import com.sos.jobstreams.resolver.interfaces.IJSCondition;
import com.sos.joc.exceptions.JocException;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

public class JSConditionResolver {

    private static final String JOB = "job";
    private static final String JOB_CHAIN = "jobchain";
    private static final Logger LOGGER = LoggerFactory.getLogger(JSConditionResolver.class);
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private JSJobInConditions jsJobInConditions;
    private JSJobOutConditions jsJobOutConditions;
    private JSEvents jsEvents;
    private SOSHibernateSession sosHibernateSession;
    private BooleanExp booleanExpression;
    private EventHandlerSettings settings;
    private CheckHistoryCondition checkHistoryCondition;
    private CheckHistoryCacheRules listOfCheckHistoryChacheRules;
    private SchedulerXmlCommandExecutor schedulerXmlCommandExecutor;
    private String workingDirectory = "";
    private JSEvents newJsEvents = new JSEvents();
    private JSEvents removeJsEvents = new JSEvents();

    public JSConditionResolver(SOSHibernateSession sosHibernateSession, SchedulerXmlCommandExecutor schedulerXmlCommandExecutor,
            EventHandlerSettings settings) {
        super();
        this.schedulerXmlCommandExecutor = schedulerXmlCommandExecutor;
        booleanExpression = new BooleanExp("");
        this.sosHibernateSession = sosHibernateSession;
        this.settings = settings;
        checkHistoryCondition = new CheckHistoryCondition(settings.getSchedulerId());
    }

    public JSConditionResolver(SOSHibernateSession sosHibernateSession, String schedulerId) {
        super();
        booleanExpression = new BooleanExp("");
        this.sosHibernateSession = sosHibernateSession;
        this.settings = new EventHandlerSettings();
        settings.setSchedulerId(schedulerId);
        checkHistoryCondition = new CheckHistoryCondition(settings.getSchedulerId());
    }

    public void reInit() throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver reinit jobstream model");
        jsJobInConditions = null;
        jsJobOutConditions = null;
        jsEvents = null;
        checkHistoryCondition = new CheckHistoryCondition(settings.getSchedulerId());
        this.init();
    }

    public void reInitEvents(SOSHibernateFactory reportingFactory) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver reinit events injobstream model");
        SOSHibernateSession session = null;
        try {
            session = reportingFactory.openStatelessSession("reInitEvents");
            jsEvents = null;
            this.initEvents(session);
        } catch (Exception e) {
            LOGGER.error("Could not reInit Events", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public void reinitCalendarUsage() {
        for (JSInConditions jsInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
            for (JSInCondition jsInCondition : jsInConditions.getListOfInConditions().values()) {
                jsInCondition.setListOfDates(sosHibernateSession, settings.getSchedulerId());
            }
        }
    }

    public boolean haveGlobalEvents() {
        return this.jsJobInConditions.getHaveGlobalConditions();
    }

    public void init() throws SOSHibernateException {

        LOGGER.debug("JSConditionResolver::Init");
        DurationCalculator duration = new DurationCalculator();

        if (jsJobInConditions == null) {
            FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
            filterConsumedInConditions.setJobSchedulerId(settings.getSchedulerId());
            filterConsumedInConditions.setSession(Constants.getSession());
            DBLayerConsumedInConditions dbLayerCoumsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);
            List<DBItemConsumedInCondition> listOfConsumedInConditions = dbLayerCoumsumedInConditions.getConsumedInConditionsList(
                    filterConsumedInConditions, 0);
            HashMap<Long, DBItemConsumedInCondition> mapOfConsumedInCondition = new HashMap<Long, DBItemConsumedInCondition>();
            for (DBItemConsumedInCondition dbItemConsumedInCondition : listOfConsumedInConditions) {
                mapOfConsumedInCondition.put(dbItemConsumedInCondition.getInConditionId(), dbItemConsumedInCondition);
            }

            FilterInConditions filterInConditions = new FilterInConditions();
            filterInConditions.setJobSchedulerId(settings.getSchedulerId());

            DBLayerInConditions dbLayerInConditions = new DBLayerInConditions(sosHibernateSession);
            List<DBItemInConditionWithCommand> listOfInConditions = dbLayerInConditions.getInConditionsList(filterInConditions, 0);
            for (DBItemInConditionWithCommand dbItemInCondition : listOfInConditions) {
                DBItemInCondition inInCondition = dbItemInCondition.getDbItemInCondition();
                if (inInCondition != null) {
                    boolean isConsumed = (mapOfConsumedInCondition.get(dbItemInCondition.getDbItemInCondition().getId()) != null);
                    if (isTraceEnabled) {
                        LOGGER.trace("Expression " + inInCondition.getExpression() + " in " + inInCondition.getJob() + " is set to consumed="
                                + isConsumed);
                    }
                    dbItemInCondition.setConsumed((mapOfConsumedInCondition.get(dbItemInCondition.getDbItemInCondition().getId()) != null));
                } else {
                    dbItemInCondition.setConsumed(false);
                }
            }
            jsJobInConditions = new JSJobInConditions(settings);
            jsJobInConditions.setListOfJobInConditions(sosHibernateSession, listOfInConditions);

        }

        if (jsJobOutConditions == null) {
            FilterOutConditions filterOutConditions = new FilterOutConditions();
            filterOutConditions.setJobSchedulerId(settings.getSchedulerId());
            DBLayerOutConditions dbLayerOutConditions = new DBLayerOutConditions(sosHibernateSession);
            List<DBItemOutConditionWithConfiguredEvent> listOfOutConditions = dbLayerOutConditions.getOutConditionsList(filterOutConditions, 0);
            jsJobOutConditions = new JSJobOutConditions();
            jsJobOutConditions.setListOfJobOutConditions(listOfOutConditions);
        }

        if (listOfCheckHistoryChacheRules == null) {
            listOfCheckHistoryChacheRules = new CheckHistoryCacheRules();
            listOfCheckHistoryChacheRules.initCacheRules();
        }

        initEvents(sosHibernateSession);
        initCheckHistory();
        if (isDebugEnabled) {
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

    public void reInitConsumedInConditions() throws SOSHibernateException {

        LOGGER.debug("JSConditionResolver::reInitConsumedInConditions");
        FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
        filterConsumedInConditions.setJobSchedulerId(settings.getSchedulerId());
        filterConsumedInConditions.setSession(Constants.getSession());
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
                if (inCondition.isConsumed() && mapOfConsumedInCondition.get(inCondition.getId()) == null) {
                    DBItemConsumedInCondition dbItemConsumedInCondition = new DBItemConsumedInCondition();
                    dbItemConsumedInCondition.setCreated(new Date());
                    dbItemConsumedInCondition.setInConditionId(inCondition.getId());
                    dbItemConsumedInCondition.setSession(Constants.getSession());
                    dbLayerCoumsumedInConditions.store(dbItemConsumedInCondition);
                    if (isTraceEnabled) {
                        LOGGER.trace(String.format("Consumed In Condition stored %s", inCondition.toStr()));
                    }

                }
                if (!inCondition.isConsumed() && mapOfConsumedInCondition.get(inCondition.getId()) != null) {
                    filterConsumedInConditions.setInConditionId(inCondition.getId());
                    dbLayerCoumsumedInConditions.deleteByInConditionId(filterConsumedInConditions);
                    if (isTraceEnabled) {
                        LOGGER.trace(String.format("Consumed In Condition deleted %s", inCondition.toStr()));
                    }
                }
            }

        }
        sosHibernateSession.commit();
    }

    public void initCheckHistory() throws SOSHibernateException {

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

    public boolean validate(Integer taskReturnCode, IJSCondition condition) {
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
                jsEventKey.setSession(eventDate.getEventDate(date));
                jsEventKey.setGlobalEvent(jsCondition.typeIsGlobalEvent());
                jsEventKey.setSchedulerId(settings.getSchedulerId());
                jsEventKey.setJobStream(jsCondition.getConditionJobStream());
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

    public List<JSInCondition> resolveInConditions(SOSHibernateSession session) throws UnsupportedEncodingException, MalformedURLException,
            InterruptedException, SOSException, JocException, URISyntaxException, JAXBException {

        List<JSInCondition> listOfValidatedInconditions = new ArrayList<JSInCondition>();
        if (jsJobInConditions != null && jsJobInConditions.getListOfJobInConditions().size() == 0) {
            LOGGER.debug("No in conditions defined. Nothing to do");

        } else {

            LOGGER.debug("JSConditionResolver::resolveInConditions");
            for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
                for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                    String logPrompt = "";
                    if (isTraceEnabled) {
                        logPrompt = "job: " + inCondition.getJob() + " Job Stream: " + inCondition.getJobStream() + " Expression: " + inCondition
                                .getExpression();
                    }
                    if (!inCondition.isConsumed()) {
                        if (!inCondition.jobIsRunning()) {

                            if (isTraceEnabled) {
                                LOGGER.trace("---InCondition is: " + inCondition.toStr());
                            }
                            if (validate(null, inCondition)) {
                                String startedJob = inCondition.executeCommand(session, schedulerXmlCommandExecutor);
                                if (!startedJob.isEmpty()) {
                                    this.disableInconditionsForJob(settings.getSchedulerId(), startedJob);
                                }
                                listOfValidatedInconditions.add(inCondition);
                            } else {
                                if (isTraceEnabled) {
                                    LOGGER.trace(logPrompt + " evaluated to --> false");
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
        }
        return listOfValidatedInconditions;

    }

    public boolean resolveOutConditions(Integer taskReturnCode, String jobSchedulerId, String job) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver::resolveOutConditions for job:" + job);
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

            if (jobOutConditions != null) {
                for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                    String expression = outCondition.getJob() + ":" + outCondition.getExpression();

                    LOGGER.trace("---OutCondition: " + expression);
                    if (validate(taskReturnCode, outCondition)) {
                        LOGGER.trace("create/remove events ------>");
                        dbChange = outCondition.storeOutConditionEvents(sosHibernateSession, jsEvents, newJsEvents, removeJsEvents);
                    } else {
                        LOGGER.trace(expression + "-->false");
                    }
                    LOGGER.trace("");
                }
            } else {
                LOGGER.debug("No out conditions for job " + job + " found. Nothing to do");
            }
        }
        return dbChange;
    }

    public void setJobIsRunningInconditionsForJob(boolean value, String jobSchedulerId, String job) throws SOSHibernateException {
        JSJobConditionKey jobConditionKey = new JSJobConditionKey();
        jobConditionKey.setJob(job);
        jobConditionKey.setJobSchedulerId(jobSchedulerId);
        JSInConditions jobInConditions = jsJobInConditions.getListOfJobInConditions().get(jobConditionKey);
        if (jobInConditions != null && jobInConditions.getListOfInConditions().size() == 0) {
            LOGGER.debug("No in conditions defined. Nothing to do");
        } else {
            if (jobInConditions != null) {
                for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                    inCondition.setJobIsRunning(value);
                    if (isTraceEnabled) {
                        if (value) {
                            LOGGER.trace("In conditions for job " + job + " disabled");
                        }else {
                            LOGGER.trace("In conditions for job " + job + " enabled");
                        }
                    }
                }
            } else {
                LOGGER.debug("No in conditions for job " + job + " found. Nothing to do");
            }
        }
    }


    public void enableInconditionsForJob(String jobSchedulerId, String job) throws SOSHibernateException {
        setJobIsRunningInconditionsForJob(false, jobSchedulerId, job);
    }
    
    public void disableInconditionsForJob(String jobSchedulerId, String job) throws SOSHibernateException {
        setJobIsRunningInconditionsForJob(true, jobSchedulerId, job);
    }

    
    public BooleanExp getBooleanExpression() {
        return booleanExpression;
    }

    public void removeConsumedInconditions(FilterConsumedInConditions filterConsumedInConditions) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver::removeConsumedInconditions --> " + filterConsumedInConditions.getJobStream() + "."
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

    public void addEvent(JSEvent event) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolver::addEvent --> " + event.getJobStream() + "." + event.getEvent());
        this.newJsEvents = new JSEvents();
        event.store(sosHibernateSession);
        jsEvents.addEvent(event);
        newJsEvents.addEvent(event);
        LOGGER.debug(event.getEvent() + " added");
    }

    public void removeEvent(JSEvent event) throws SOSHibernateException {
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

    public void checkHistoryCache(String jobPath, Integer taskReturnCode) throws Exception {
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

    public void setReportingSession(SOSHibernateSession reportingSession) {
        this.sosHibernateSession = reportingSession;
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

}
