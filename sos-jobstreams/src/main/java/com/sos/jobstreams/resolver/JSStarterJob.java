package com.sos.jobstreams.resolver;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.checkhistory.HistoryHelper;
import com.sos.jitl.jobstreams.Constants;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarterJob;

public class JSStarterJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSStarterJob.class);

    private DBItemJobStreamStarterJob dbItemJobStreamStarterJob;
    private Set<LocalDate> listOfDates;

    public JSStarterJob() {
        super();
        this.listOfDates = new HashSet<LocalDate>();
    }

    public DBItemJobStreamStarterJob getDbItemJobStreamStarterJob() {
        return dbItemJobStreamStarterJob;
    }

    public void setDbItemJobStreamStarterJob(DBItemJobStreamStarterJob dbItemJobStreamStarterJob) {
        this.dbItemJobStreamStarterJob = dbItemJobStreamStarterJob;
    }

    public Set<LocalDate> getListOfDates() {
        return listOfDates;
    }

    public void setListOfDates(SOSHibernateSession sosHibernateSession, Set<LocalDate> listOfDates) {

        if (listOfDates != null) {
            this.listOfDates = listOfDates;

            try {
                this.setNextPeriod(sosHibernateSession);
            } catch (SOSHibernateException e) {
                LOGGER.error("Could not set the next period", e);
            }
        }

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

    protected void setNextPeriod(SOSHibernateSession sosHibernateSession) throws SOSHibernateException {
        LOGGER.debug("Setting next period for job: " + this.dbItemJobStreamStarterJob.getJob());
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate last = LocalDate.of(2099, Month.JANUARY, 1);
        LocalDate next = null;

        for (LocalDate d : this.listOfDates) {

            if (d.isBefore(last) && (d.isAfter(today) || HistoryHelper.isToday(d))) {
                last = d;
                next = d;
            }
        }
        this.dbItemJobStreamStarterJob.setNextPeriod(null);
        if (next != null) {// Empty if today.
            if (next.isAfter(today)) {

                Instant nextPeriod = last.atStartOfDay(ZoneId.of(Constants.settings.getTimezone())).toInstant();
                Date d = new Date(nextPeriod.toEpochMilli());
                LOGGER.debug("Setting next period:" + nextPeriod.toString());
                this.dbItemJobStreamStarterJob.setNextPeriod(d);
            } else {
                LOGGER.debug(next.toString() + " is not after " + today.toString());
            }

            try {
                sosHibernateSession.beginTransaction();
                sosHibernateSession.update(dbItemJobStreamStarterJob);
                sosHibernateSession.commit();
            } catch (Exception e) {
                sosHibernateSession.rollback();
            }
        }
    }

}
