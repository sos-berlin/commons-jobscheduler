package com.sos.tools.logback.db;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateFilter;

public class LoggingEventExceptionFilter extends SOSHibernateFilter implements com.sos.hibernate.interfaces.ISOSHibernateFilter {

    private Long eventId;
    private Long i;
    private String traceLine;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getI() {
        return i;
    }

    public void setI(Long i) {
        this.i = i;
    }

    public String getTraceLine() {
        return traceLine;
    }

    public void setTraceLine(String traceLine) {
        this.traceLine = traceLine;
    }

    @Override
    public boolean isFiltered(DbItem dbItem) {
        return false;
    }

    @Override
    public String getTitle() {
        return null;
    }
}
