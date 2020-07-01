package com.sos.jobstreams.resolver;

import java.time.LocalDate;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jitl.checkhistory.HistoryHelper;
import com.sos.jitl.jobstreams.db.DBItemJobStreamStarterJob;

public class JSStarterJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSStarterJob.class);

    private DBItemJobStreamStarterJob dbItemJobStreamStarterJob;
    private Set<LocalDate> listOfDates;

    public DBItemJobStreamStarterJob getDbItemJobStreamStarterJob() {
        return dbItemJobStreamStarterJob;
    }

    public void setDbItemJobStreamStarterJob(DBItemJobStreamStarterJob dbItemJobStreamStarterJob) {
        this.dbItemJobStreamStarterJob = dbItemJobStreamStarterJob;
    }

    public Set<LocalDate> getListOfDates() {
        return listOfDates;
    }

   
    public void setListOfDates(Set<LocalDate> listOfDates) {
        this.listOfDates = listOfDates;
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

 
}
