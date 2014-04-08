package com.sos.tools.logback.db;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateFilter;

public class LoggingEventFilter extends SOSHibernateFilter implements com.sos.hibernate.interfaces.ISOSHibernateFilter {

    private Long eventId;
    private String loggerName;
    private String mappedValue;
    private String mappedKey;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getMappedValue() {
        return mappedValue;
    }

    public void setMappedValue(String mappedValue) {
        this.mappedValue = mappedValue;
    }

    public String getMappedKey() {
        return mappedKey;
    }

    public void setMappedKey(String mappedKey) {
        this.mappedKey = mappedKey;
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
