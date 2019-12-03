package com.sos.jobstreams.resolver;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.checkhistory.HistoryHelper;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jitl.jobstreams.db.DBItemConsumedInCondition;
import com.sos.jitl.jobstreams.db.DBItemInCondition;
import com.sos.jitl.jobstreams.db.DBLayerConsumedInConditions;
import com.sos.jitl.jobstreams.db.FilterCalendarUsage;
import com.sos.jitl.jobstreams.interfaces.IJSJobConditionKey;
import com.sos.jobstreams.classes.JobStreamCalendar;
import com.sos.jobstreams.resolver.interfaces.IJSCondition;
import com.sos.joc.exceptions.JocException;
import com.sos.scheduler.engine.kernel.scheduler.SchedulerXmlCommandExecutor;

import sos.util.SOSString;

public class JSInCondition implements IJSJobConditionKey, IJSCondition {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSInCondition.class);
    private DBItemInCondition itemInCondition;
    private List<JSInConditionCommand> listOfInConditionCommands;
    private boolean consumed;
    private String normalizedJob;
    private Set<LocalDate> listOfDates;
    private boolean haveCalendars;

    public JSInCondition() {
        super();
        haveCalendars = false;
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

    protected void markAsConsumed(SOSHibernateSession sosHibernateSession) throws SOSHibernateException {
        this.consumed = true;
        DBItemConsumedInCondition dbItemConsumedInCondition = new DBItemConsumedInCondition();
        dbItemConsumedInCondition.setCreated(new Date());
        dbItemConsumedInCondition.setInConditionId(this.getId());
        dbItemConsumedInCondition.setSession(Constants.getSession());
        try {
            DBLayerConsumedInConditions dbLayerConsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);
            sosHibernateSession.beginTransaction();
            dbLayerConsumedInConditions.deleteInsert(dbItemConsumedInCondition);
            sosHibernateSession.commit();
        } catch (Exception e) {
            sosHibernateSession.rollback();
        }
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public void setListOfDates(SOSHibernateSession sosHibernateSession, String schedulerId) {
        FilterCalendarUsage filterCalendarUsage = new FilterCalendarUsage();
        filterCalendarUsage.setPath(normalizedJob);
        filterCalendarUsage.setSchedulerId(schedulerId);
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
        } catch (Exception e) {
            LOGGER.error("could not read the list of dates: " + SOSString.toString(filterCalendarUsage), e);
        }

    }

    public void executeCommand(SOSHibernateSession sosHibernateSession, SchedulerXmlCommandExecutor schedulerXmlCommandExecutor)
            throws SOSHibernateException, JocException, JAXBException {
        LOGGER.trace("execute commands ------>");
        if (this.isMarkExpression()) {
            LOGGER.trace("Expression: " + this.getExpression() + " now marked as consumed");
            this.markAsConsumed(sosHibernateSession);
        }

        for (JSInConditionCommand inConditionCommand : this.getListOfInConditionCommand()) {
            inConditionCommand.executeCommand(schedulerXmlCommandExecutor, this);
        }
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

}
