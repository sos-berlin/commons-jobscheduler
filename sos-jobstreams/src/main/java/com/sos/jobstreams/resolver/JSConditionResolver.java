package com.sos.jobstreams.resolver;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSException;
import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.eventhandler.handler.EventHandlerSettings;
import com.sos.jitl.eventing.evaluate.BooleanExp;
import com.sos.jobstreams.classes.CheckHistoryCacheRule;
import com.sos.jobstreams.classes.CheckHistoryCondition;
import com.sos.jobstreams.classes.CheckHistoryKey;
import com.sos.jobstreams.classes.CheckHistoryValue;
import com.sos.jobstreams.classes.Constants;
import com.sos.jobstreams.classes.DurationCalculator;
import com.sos.jobstreams.classes.EventDate;
import com.sos.jobstreams.db.DBItemConsumedInCondition;
import com.sos.jobstreams.db.DBItemEvent;
import com.sos.jobstreams.db.DBItemInCondition;
import com.sos.jobstreams.db.DBItemInConditionWithCommand;
import com.sos.jobstreams.db.DBItemOutConditionWithEvent;
import com.sos.jobstreams.db.DBLayerConsumedInConditions;
import com.sos.jobstreams.db.DBLayerEvents;
import com.sos.jobstreams.db.DBLayerInConditions;
import com.sos.jobstreams.db.DBLayerOutConditions;
import com.sos.jobstreams.db.FilterConsumedInConditions;
import com.sos.jobstreams.db.FilterEvents;
import com.sos.jobstreams.db.FilterInConditions;
import com.sos.jobstreams.db.FilterOutConditions;
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
    private JSJobOutConditions jsJobOutConditions;
    private JSEvents jsEvents;
    private SOSHibernateSession sosHibernateSession;
    private BooleanExp booleanExpression;
    private EventHandlerSettings settings;
    private CheckHistoryCondition checkHistoryCondition;
    private List<CheckHistoryCacheRule> listOfCheckHistoryChacheRules;
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
        sosHibernateSession.beginTransaction();
        sosHibernateSession.commit();

        jsEvents = null;
        checkHistoryCondition = new CheckHistoryCondition(settings.getSchedulerId());
        this.init();
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
                        LOGGER.trace(inInCondition.getExpression() + " in " + inInCondition.getJob() + " is set to:" + isConsumed);
                    }
                    dbItemInCondition.setConsumed((mapOfConsumedInCondition.get(dbItemInCondition.getDbItemInCondition().getId()) != null));
                } else {
                    dbItemInCondition.setConsumed(false);
                }
            }
            jsJobInConditions = new JSJobInConditions(settings);
            jsJobInConditions.setListOfJobInConditions(listOfInConditions);

        }

        if (jsJobOutConditions == null) {
            FilterOutConditions filterOutConditions = new FilterOutConditions();
            filterOutConditions.setJobSchedulerId(settings.getSchedulerId());
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
        if (isDebugEnabled) {
            duration.end("Init JobStreams condition model ");
            LOGGER.debug("In-Conditions: " + jsJobInConditions.getListOfJobInConditions().size());
            LOGGER.debug("Out-Conditions: " + jsJobOutConditions.getListOfJobOutConditions().size());
            LOGGER.debug("Events: " + jsEvents.getListOfEvents().size());
        }

    }

    public void initEvents() throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::initEvents");
        if (jsEvents == null) {
            jsEvents = new JSEvents();
            FilterEvents filterEvents = new FilterEvents();
            filterEvents.setSchedulerId(settings.getSchedulerId());
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            List<DBItemEvent> listOfEvents = dbLayerEvents.getEventsList(filterEvents, 0);
            jsEvents.setListOfEvents(listOfEvents);
        }
    }

    public void initCheckHistory() throws SOSHibernateException {

        LOGGER.debug("JSConditionResolve::initCheckHistory");
        for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
            for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                String expressionValue = inCondition.getExpression();
                JSConditions jsConditions = new JSConditions();
                List<JSCondition> listOfConditions = jsConditions.getListOfConditions(expressionValue);
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
                        LOGGER.warn("Could not validate expression:" + expressionValue + " in in-condition of job " + inCondition.getJob());
                    }
                }
            }
        }

        for (JSOutConditions jobOutConditions : jsJobOutConditions.getListOfJobOutConditions().values()) {
            for (JSOutCondition outCondition : jobOutConditions.getListOfOutConditions().values()) {
                String expressionValue = outCondition.getExpression() + " ";
                JSConditions jsConditions = new JSConditions();
                List<JSCondition> listOfConditions = jsConditions.getListOfConditions(expressionValue);
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
                        LOGGER.warn("Could not validate expression:" + expressionValue + " in in-condition of job " + outCondition.getJob());
                    }
                }
            }
        }
    }

    private String expressionPrepare(String e) {
        return e.replaceAll("\\(", "###(###").replaceAll("\\)", "###)###").replaceAll(" and ", "###&&&###").replaceAll(" or ", "###|||###")
                .replaceAll("not ", "###!!!###");
    }

    private String expressionBack(String e) {
        return e.replaceAll("\\#\\#\\#\\(\\#\\#\\#", "(").replaceAll("\\#\\##\\)\\#\\#\\#", ")").replaceAll("\\#\\#\\#\\&\\&\\&\\#\\#\\#", " and ")
                .replaceAll("\\#\\#\\#\\|\\|\\|\\#\\#\\#", " or ").replaceAll("\\#\\#\\#\\!\\!\\!\\#\\#\\#", "not ");

    }

    public boolean validate(Integer taskReturnCode, IJSCondition condition) {
        String expressionValue = condition.getExpression() + " ";
        JSConditions jsConditions = new JSConditions();
        List<JSCondition> listOfConditions = jsConditions.getListOfConditions(expressionValue);
        LOGGER.trace("JSConditionResolve::validate");
        for (JSCondition jsCondition : listOfConditions) {
            LOGGER.trace("JSConditionResolve::validate --> " + jsCondition.getConditionType());

            switch (jsCondition.getConditionType()) {
            case "rc":
            case "returncode": {
                JSReturnCodeResolver returnCodeResolver = new JSReturnCodeResolver();
                if (returnCodeResolver.resolve(taskReturnCode, jsCondition.getConditionParam())) {
                    expressionValue = this.expressionPrepare(expressionValue);
                    expressionValue = expressionValue.replace(jsCondition.getConditionValue(), "true");
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
                    expressionValue = expressionValue.replace(jsCondition.getConditionValue(), "true");
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
                        expressionValue = expressionValue.replace(jsCondition.getConditionValue(), "true");
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
                        expressionValue = expressionValue.replace(jsCondition.getConditionValue(), "true");
                        expressionValue = this.expressionBack(expressionValue);

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
                    expressionValue = this.expressionPrepare(expressionValue);
                    expressionValue = expressionValue.replace(jsCondition.getConditionValue(), "true");
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
        LOGGER.trace(condition.getExpression() + "evaluated to: " + evaluatedExpression);
        return evaluatedExpression;
    }

    public List<JSInCondition> resolveInConditions() throws UnsupportedEncodingException, MalformedURLException, InterruptedException, SOSException,
            URISyntaxException {

        List<JSInCondition> listOfValidatedInconditions = new ArrayList<JSInCondition>();
        if (jsJobInConditions != null && jsJobInConditions.getListOfJobInConditions().size() == 0) {
            LOGGER.debug("No in conditions defined. Nothing to do");

        } else {

            LOGGER.debug("JSConditionResolve::resolveInConditions");
            for (JSInConditions jobInConditions : jsJobInConditions.getListOfJobInConditions().values()) {
                for (JSInCondition inCondition : jobInConditions.getListOfInConditions().values()) {
                    String logPrompt = "";
                    if (isTraceEnabled) {
                        logPrompt = "job: " + inCondition.getJob() + " Job Stream: " + inCondition.getJobStream() + " Expression: " + inCondition
                                .getExpression();
                    }
                    if (!inCondition.isConsumed()) {

                        if (isTraceEnabled) {
                            LOGGER.trace("---InCondition is: " + SOSString.toString(inCondition));
                        }
                        if (validate(null, inCondition)) {
                            listOfValidatedInconditions.add(inCondition);
                            inCondition.executeCommand(sosHibernateSession, schedulerXmlCommandExecutor);
                        } else {
                            if (isTraceEnabled) {
                                LOGGER.trace(logPrompt + " evaluated to --> false");
                            }
                        }
                    } else {
                        if (isTraceEnabled) {
                            LOGGER.trace(logPrompt + "-->already consumed");
                        }
                    }
                }
            }
        }
        return listOfValidatedInconditions;
    }

    public void resolveOutConditions(Integer taskReturnCode, String jobSchedulerId, String job) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::resolveOutConditions for job:" + job);
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
                        outCondition.storeOutConditionEvents(sosHibernateSession, jsEvents, newJsEvents, removeJsEvents);
                    } else {
                        LOGGER.trace(expression + "-->false");
                    }
                    LOGGER.trace("");
                }
            } else {
                LOGGER.debug("No out conditions for job: " + job + " found. Nothing to do");
            }
        }
    }

    public BooleanExp getBooleanExpression() {
        return booleanExpression;
    }

    public void removeConsumedInconditions(FilterConsumedInConditions filterConsumedInConditions) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::removeConsumedInconditions --> " + filterConsumedInConditions.getJobStream() + "."
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
        LOGGER.debug("JSConditionResolve::removeEventsFromJobStream --> " + filter.getJobStream() + "." + filter.getJob());
        try {
            DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerEvents.deleteEventsWithOutConditions(filter);
            sosHibernateSession.commit();

            jsEvents = null;
            initEvents();
        } catch (

        Exception e) {
            LOGGER.error(e.getMessage(), e);
            sosHibernateSession.rollback();
        }
    }

    public void addEvent(JSEvent event) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::addEvent --> " + event.getJobStream() + "." + event.getEvent());
        this.newJsEvents = new JSEvents();
        event.store(sosHibernateSession);
        jsEvents.addEvent(event);
        newJsEvents.addEvent(event);
        LOGGER.debug(event.getEvent() + " added");
    }

    public void removeEvent(JSEvent event) throws SOSHibernateException {
        LOGGER.debug("JSConditionResolve::removeEvent --> " + event.getJobStream() + "." + event.getEvent());
        this.removeJsEvents = new JSEvents();
        removeJsEvents.addEvent(event);
        event.deleteEvent(sosHibernateSession);
        jsEvents.removeEvent(event);
        LOGGER.debug(event.getEvent() + " removed");
    }

    public Boolean eventExist(JSEventKey jsEventKey, String jobStream) {
        JSEvent jsEvent = jsEvents.getEventByJobStream(jsEventKey, jobStream);
        return jsEvent != null;
    }

    public void checkHistoryCache(String jobPath, Integer taskReturnCode) throws Exception {
        CheckHistoryKey checkHistoryKey = new CheckHistoryKey(JOB, jobPath, "");
        if (listOfCheckHistoryChacheRules == null) {
            LOGGER.warn("History not initialized");
        } else {
            for (CheckHistoryCacheRule checkHistoryCacheRule : listOfCheckHistoryChacheRules) {
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

    public JSEvents getRemoveJsEvents() {
        return removeJsEvents;
    }
}
