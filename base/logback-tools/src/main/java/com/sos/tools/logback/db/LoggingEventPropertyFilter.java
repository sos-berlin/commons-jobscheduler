package com.sos.tools.logback.db;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateFilter;

public class LoggingEventPropertyFilter extends SOSHibernateFilter implements com.sos.hibernate.interfaces.ISOSHibernateFilter {

    private Long eventId;
    private String mappedKey;
    private String mappedValue;

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    private String loggerName;

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getMappedKey() {
        return mappedKey;
    }

    public void setMappedKey(String mappedKey) {
        this.mappedKey = mappedKey;
    }

    public String getMappedValue() {
        return mappedValue;
    }

    public void setMappedValue(String mappedValue) {
        this.mappedValue = mappedValue;
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
