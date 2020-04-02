package com.sos.jobstreams.resolver;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
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
import com.sos.jitl.jobstreams.db.DBItemConsumedInCondition;
import com.sos.jitl.jobstreams.db.DBItemInCondition;
import com.sos.jitl.jobstreams.db.DBLayerConsumedInConditions;
import com.sos.jitl.jobstreams.db.FilterCalendarUsage;
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
    private Map<UUID,Boolean> listOfRunningJobs;
    private String normalizedJob;
    private Set<LocalDate> listOfDates;
    private boolean haveCalendars;

    public JSInCondition() {
        super();
        haveCalendars = false;
        listOfRunningJobs = new HashMap<UUID,Boolean>();
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
        LocalDate today = LocalDate.now();
        LocalDate last = LocalDate.of(2099, Month.JANUARY, 1);
        LocalDate next = null;

        for (LocalDate d : this.listOfDates) {

            if (d.isBefore(last) && (d.isAfter(today) || d.getDayOfYear() == today.getDayOfYear())) {
                last = d;
                next = d;
            }
        }

        this.itemInCondition.setNextPeriod(null);
        if (next != null && next.isAfter(today)) {// Empty if today.
            this.itemInCondition.setNextPeriod(Date.from(last.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        try {
            sosHibernateSession.beginTransaction();
            sosHibernateSession.update(this.itemInCondition);
            sosHibernateSession.commit();
        } catch (Exception e) {
            sosHibernateSession.rollback();
        }

    }

    public boolean isConsumed(UUID contextId) {
        LOGGER.debug("check consumed " + this.getExpression() + " is consumed for context " + contextId + " ---> " + consumedForContext.contains(contextId));
        return consumedForContext.contains(contextId);
    }

    public void setConsumed(UUID contextId) {
        LOGGER.debug(this.getExpression() + " is consumed for context " + contextId);
        consumedForContext.add(contextId);
    }

    public void setListOfDates(SOSHibernateSession sosHibernateSession, String schedulerId) {
        FilterCalendarUsage filterCalendarUsage = new FilterCalendarUsage();
        filterCalendarUsage.setPath(normalizedJob);
        filterCalendarUsage.setSchedulerId(schedulerId);
        this.listOfDates = new HashSet<LocalDate>();

        JobStreamCalendar jobStreamCalendar = new JobStreamCalendar();
        Set<LocalDate> l;

        try {
            l = jobStreamCalendar.getListOfDates(sosHibernateSession, filterCalendarUsage);
            if (l == null) {
                haveCalendars = false;
            } else {
                haveCalendars = true;
                this.listOfDates.addAll(l);
            }

            try {
                this.setNextPeriod(sosHibernateSession);
            } catch (SOSHibernateException e) {
                LOGGER.error("Could not set the next period", e);
            }
        } catch (Exception e) {
            LOGGER.error("could not read the list of dates: " + SOSString.toString(filterCalendarUsage), e);
        }

    }

    public StartJobReturn executeCommand(SOSHibernateSession sosHibernateSession, UUID contextId,
            Map<String, String> listOfParameters, SchedulerXmlCommandExecutor schedulerXmlCommandExecutor) throws NumberFormatException, Exception {
        LOGGER.trace("execute commands ------>");
        StartJobReturn startJobReturn = new StartJobReturn();
        startJobReturn.setStartedJob("");
        if (this.isMarkExpression()) {
            LOGGER.trace("Expression: " + this.getExpression() + " now marked as consumed");
            this.markAsConsumed(sosHibernateSession, contextId);
        }

        for (JSInConditionCommand inConditionCommand : this.getListOfInConditionCommand()) {
            startJobReturn = inConditionCommand.executeCommand(schedulerXmlCommandExecutor, this,listOfParameters);
        }
        return startJobReturn;
    }

    public boolean isStartToday() {
        if (!haveCalendars) {
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
        this.listOfRunningJobs.put(contextId,jobIsRunning);
    }

    public Date getNextPeriod() {
        return this.itemInCondition.getNextPeriod();
    }

    public void removeConsumed(UUID contextId) {
        consumedForContext.remove(contextId);
    }

}
