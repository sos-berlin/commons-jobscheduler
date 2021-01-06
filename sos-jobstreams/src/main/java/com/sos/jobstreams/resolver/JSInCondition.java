package com.sos.jobstreams.resolver;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.checkhistory.HistoryHelper;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jitl.jobstreams.db.DBItemCalendarWithUsages;
import com.sos.jitl.jobstreams.db.DBItemConsumedInCondition;
import com.sos.jitl.jobstreams.db.DBItemInCondition;
import com.sos.jitl.jobstreams.db.DBLayerConsumedInConditions;
import com.sos.jitl.jobstreams.interfaces.IJSJobConditionKey;
import com.sos.jobstreams.classes.JobStreamCalendar;
import com.sos.jobstreams.classes.StartJobReturn;
import com.sos.jobstreams.resolver.interfaces.IJSCondition;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

import sos.util.SOSString;

public class JSInCondition implements IJSJobConditionKey, IJSCondition {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSInCondition.class);
    private DBItemInCondition itemInCondition;
    private List<JSInConditionCommand> listOfInConditionCommands;
    private Set<UUID> consumedForContext;
    private Map<UUID, Boolean> listOfRunningJobs;
    private boolean haveOnlyInstanceEvents=true;
    private String normalizedJob;
    private Set<LocalDate> listOfDates;
    private UUID evaluatedContextId;
    
  
    public JSInCondition() {
        super();
        listOfRunningJobs = new HashMap<UUID, Boolean>();
        this.consumedForContext = new HashSet<UUID>();
        this.listOfDates = new HashSet<LocalDate>();
        this.listOfInConditionCommands = new ArrayList<JSInConditionCommand>();
    }

    private String normalizePath(String path) {
        if (path == null) {
            return null;
        }
        return ("/" + path.trim()).replaceAll("//+", "/").replaceFirst("/$", "");
    }

    public void setItemInCondition(DBItemInCondition itemInCondition) {
        this.itemInCondition = itemInCondition;
        this.normalizedJob = normalizePath(itemInCondition.getJob());
        
        String expressionValue = getExpression() + " ";
        expressionValue = JSConditions.normalizeExpression(expressionValue);
        List<JSCondition> listOfConditions = JSConditions.getListOfConditions(expressionValue);
        for (JSCondition jsCondition : listOfConditions) {
            if (jsCondition.isGlobalEvent() || jsCondition.isNonContextEvent() || !"event".equals(jsCondition.getConditionType()) ){
                haveOnlyInstanceEvents = false;
                break;
            }
        }

        
    }

    public Long getId() {
        return itemInCondition.getId();
    }

    public String getNormalizedJob() {
        return normalizedJob;
    }

    public String getJobSchedulerId() {
        return itemInCondition.getSchedulerId();
    }

    public String getJob() {
        return itemInCondition.getJob();
    }

    public String getExpression() {
        return itemInCondition.getExpression().replaceAll("\\s*\\[", "[") + " ";
    }

    public String getJobStream() {
        return itemInCondition.getJobStream();
    }

    public boolean isMarkExpression() {
        return itemInCondition.getMarkExpression();
    }

    public boolean isSkipOutCondition() {
        return itemInCondition.getSkipOutCondition();
    }

    public void addCommand(JSInConditionCommand inConditionCommand) {
        listOfInConditionCommands.add(inConditionCommand);
    }

    public List<JSInConditionCommand> getListOfInConditionCommand() {
        return listOfInConditionCommands;
    }

    protected void markAsConsumed(SOSHibernateSession sosHibernateSession, UUID contextId) throws SOSHibernateException {
        setConsumed(contextId);
        DBItemConsumedInCondition dbItemConsumedInCondition = new DBItemConsumedInCondition();
        dbItemConsumedInCondition.setCreated(new Date());
        dbItemConsumedInCondition.setInConditionId(this.getId());
        dbItemConsumedInCondition.setSession(String.valueOf(contextId));
        DBLayerConsumedInConditions dbLayerConsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);
        dbLayerConsumedInConditions.deleteInsert(dbItemConsumedInCondition);
    }

    protected void setNextPeriod(SOSHibernateSession sosHibernateSession) throws SOSHibernateException {
        LOGGER.debug("Setting next period for job: " + this.getJob() + " expression: " + this.getExpression());
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate last = LocalDate.of(2099, Month.JANUARY, 1);
        LocalDate next = null;

        for (LocalDate d : this.listOfDates) {

            if (d.isBefore(last) && (d.isAfter(today) || HistoryHelper.isToday(d))) {
                last = d;
                next = d;
            }
        }
        this.itemInCondition.setNextPeriod(null);
        if (next != null) {// Empty if today.
            if (next.isAfter(today)) {

                Instant nextPeriod = last.atStartOfDay(ZoneId.of(Constants.settings.getTimezone())).toInstant();
                Date d = new Date(nextPeriod.toEpochMilli());
                LOGGER.debug("Setting next period:" + nextPeriod.toString());
                this.itemInCondition.setNextPeriod(d);
            } else {
                LOGGER.debug(next.toString() + " is not after " + today.toString());
            }

            try {
                sosHibernateSession.beginTransaction();
                if (this.itemInCondition.getCreated() == null) {
                    this.itemInCondition.setCreated(new Date());
                }
                sosHibernateSession.update(this.itemInCondition);
                sosHibernateSession.commit();
            } catch (Exception e) {
                sosHibernateSession.rollback();
            }
        }
    }

    public boolean isConsumed(UUID contextId) {
        LOGGER.debug("check consumed " + this.getExpression() + " is consumed for context " + contextId + " ---> " + consumedForContext.contains(
                contextId));
        return consumedForContext.contains(contextId);
    }

    public void setConsumed(UUID contextId) {
        LOGGER.debug(this.getExpression() + " is consumed for context " + contextId);
        consumedForContext.add(contextId);
    }

    public void setListOfDates(SOSHibernateSession sosHibernateSession, Map<String, List<DBItemCalendarWithUsages>> listOfCalendarUsages) {
        this.listOfDates = new HashSet<LocalDate>();

        JobStreamCalendar jobStreamCalendar = new JobStreamCalendar();
        Set<LocalDate> l;

        try {
            if (listOfCalendarUsages.get(normalizedJob) != null) {
                l = jobStreamCalendar.getListOfDates(listOfCalendarUsages.get(normalizedJob));
                if (l != null) {

                    this.listOfDates.addAll(l);
                }
            }

            try {
                this.setNextPeriod(sosHibernateSession);
            } catch (SOSHibernateException e) {
                LOGGER.error("Could not set the next period", e);
            }
        } catch (Exception e) {
            LOGGER.error("could not read the list of dates: " + normalizedJob, e);
        }

    }

    public StartJobReturn executeCommand(SOSHibernateSession sosHibernateSession, UUID contextId, Map<String, String> listOfParameters,
            SchedulerXmlCommandExecutor schedulerXmlCommandExecutor) throws NumberFormatException, Exception {
        LOGGER.trace("execute commands ------>");
        StartJobReturn startJobReturn = new StartJobReturn();
        startJobReturn.setStartedJob("");

        String isMark = "";
        if (!this.isMarkExpression()) {
            isMark = " and will be executed again";
        }

        for (JSInConditionCommand inConditionCommand : this.getListOfInConditionCommand()) {
            startJobReturn = inConditionCommand.executeCommand(schedulerXmlCommandExecutor, this, listOfParameters);
        }

        LOGGER.trace("Expression: " + this.getExpression() + " now marked as consumed " + isMark);

        if (startJobReturn.isStarted()) {
            this.markAsConsumed(sosHibernateSession, contextId);
        }

        return startJobReturn;
    }

    public boolean isStartToday() {
        if (listOfDates == null || listOfDates.isEmpty()) {
            return true;
        }
        for (LocalDate d : listOfDates) {
            if (HistoryHelper.isToday(d)) {
                return true;
            }
        }

        return false;
    }

    public String toStr() {
        return this.getExpression() + "::" + SOSString.toString(this);
    }

    public boolean jobIsRunning(UUID contextId) {
        Boolean b = listOfRunningJobs.get(contextId);
        if (b == null) {
            b = false;
        }
        return b;
    }

    public void setJobIsRunning(UUID contextId, boolean jobIsRunning) {
        this.listOfRunningJobs.put(contextId, jobIsRunning);
    }

    public Date getNextPeriod() {
        return this.itemInCondition.getNextPeriod();
    }

    public void removeConsumed(UUID contextId) {
        consumedForContext.remove(contextId);
    }

    public UUID getEvaluatedContextId() {
        return evaluatedContextId;
    }

    public void setEvaluatedContextId(UUID evaluatedContextId) {
        this.evaluatedContextId = evaluatedContextId;
    }

    public DBItemInCondition getItemInCondition() {
        return itemInCondition;
    }

    
    public Set<UUID> getConsumedForContext() {
        return consumedForContext;
    }

    
    public void setConsumedForContext(Set<UUID> consumedForContext) {
        this.consumedForContext = consumedForContext;
    }

    
    public Map<UUID, Boolean> getListOfRunningJobs() {
        return listOfRunningJobs;
    }

    
    public void setListOfRunningJobs(Map<UUID, Boolean> listOfRunningJobs) {
        this.listOfRunningJobs = listOfRunningJobs;
    }

    
    public boolean isHaveOnlyInstanceEvents() {
        return haveOnlyInstanceEvents;
    }

 

}
