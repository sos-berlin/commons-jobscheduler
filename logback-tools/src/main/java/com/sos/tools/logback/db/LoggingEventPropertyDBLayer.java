package com.sos.tools.logback.db;

import com.sos.hibernate.layer.SOSHibernateDBLayer;
import org.hibernate.Query;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoggingEventPropertyDBLayer extends SOSHibernateDBLayer {
	
    private static final String EVENT_ID = "eventId";
    private static final String MAPPED_KEY = "mappedKey";
    private static final String MAPPED_VALUE = "mappedValue";
    private static final String LOGGER_NAME = "loggerName";

    private LoggingEventPropertyFilter filter = null;

    public LoggingEventPropertyDBLayer(File configurationFile) {
        super();
        this.filter = new LoggingEventPropertyFilter();
        this.setConfigurationFile(configurationFile);
        this.filter.setOrderCriteria(EVENT_ID);
        initSession();
    }

    private Query setQueryParams(String hql) {
        Query query = session.createQuery(hql);

        if (filter.getEventId() != null) {
            query.setLong(EVENT_ID, filter.getEventId());
        }

        if (filter.getMappedKey() != null && !filter.getMappedKey().equals("")) {
            query.setParameter(MAPPED_KEY, filter.getMappedKey());
        }

        if (filter.getMappedValue() != null && !filter.getMappedValue().equals("")) {
            query.setParameter(MAPPED_VALUE, filter.getMappedValue());
        }

        if (filter.getLoggerName() != null && !filter.getLoggerName().equals("")) {
            query.setParameter(LOGGER_NAME, filter.getLoggerName());
        }
        return query;
    }

    private String getWhere() {
        String where = "";
        String and = "";

        if (filter.getEventId() != null) {
            where += and + " eventId = :eventId";
            and = " and ";
        }

        if (filter.getMappedKey() != null && !filter.getMappedKey().equals("")) {
            where += and + " mappedKey = :mappedKey";
            and = " and ";
        }

        if (filter.getMappedValue() != null && !filter.getMappedValue().equals("")) {
            where += and + " mappedValue = :mappedValue";
            and = " and ";
        }

        if (filter.getLoggerName() != null && !filter.getLoggerName().equals("")) {
            where += and + " events.loggingEventDBItem.loggerName = :loggerName";
            and = " and ";
        }

        return (where.isEmpty()) ? where : "where " + where;
    }

    public List<LoggingEventDBItem> getProtocol(String mappedKey, String mappedValue, String loggerName) {
        List<LoggingEventDBItem> result = new ArrayList<LoggingEventDBItem>();
        List<LoggingEventPropertyDBItem> records = getListOfPropertyDBItems(mappedKey, mappedValue, loggerName);
        for(LoggingEventPropertyDBItem item : records) {
            result.add(item.getLoggingEventDBItem());
        }
        return result;
    }

    public String asText(List<LoggingEventDBItem> records) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm:ss,SSS");
        StringBuffer result = new StringBuffer();
        for(LoggingEventDBItem record : records) {
            StringBuffer line = new StringBuffer();
            Long t = record.getTimeStmp();
            line.append( fmt.print(t) );
            line.append(" - ");
            line.append(record.getFormattedMessage());
            line.append("\n");
            result.append(line);
        }
        return result.toString();

    }

    public List<LoggingEventPropertyDBItem> getListOfPropertyDBItems(String mappedKey, String mappedValue, String loggerName) {
        filter = new LoggingEventPropertyFilter();
        filter.setOrderCriteria(EVENT_ID);
        filter.setMappedKey(mappedKey);
        filter.setMappedValue(mappedValue);
        filter.setLoggerName(loggerName);
        Query query = setQueryParams("from LoggingEventPropertyDBItem events " + getWhere() + this.filter.getOrderCriteria() + this.filter.getSortMode());
        return query.list();
    }

    /**
     * Because LOGGING_EVENT_PROPERTY works with composite eventId + mappedKey and it is not clear how hibernate handle composites directly
     * we read all records first and select the relevant afterwards.
     *
     * @param eventId
     * @return List<LoggingEventPropertyDBItem>
     */
    public List<LoggingEventPropertyDBItem> getListOfPropertyDBItems(Long eventId) {
        filter = new LoggingEventPropertyFilter();
        filter.setOrderCriteria(EVENT_ID);
        filter.setEventId(eventId);
        Query query = setQueryParams("from LoggingEventPropertyDBItem events " + getWhere() + this.filter.getOrderCriteria() + this.filter.getSortMode());
        return query.list();
    }

    public List<LoggingEventPropertyDBItem> getAll() {
        filter = new LoggingEventPropertyFilter();
        filter.setOrderCriteria(EVENT_ID);
        Query query = setQueryParams("from LoggingEventPropertyDBItem events " + this.filter.getOrderCriteria() + this.filter.getSortMode());
        return query.list();
    }

    public int deleteProtocol(String uuid, String loggerName) {
        return 0;
    }

    public int delete(Long eventId) {
        LoggingEventDBLayer eventLayer = new LoggingEventDBLayer(getConfigurationFile());
        eventLayer.delete(eventId);
        filter = new LoggingEventPropertyFilter();
        filter.setOrderCriteria(EVENT_ID);
        filter.setEventId(eventId);
        String hql = "delete from LoggingEventPropertyDBItem" + getWhere();
        Query query = setQueryParams(hql);
        int row = query.executeUpdate();
        return row;
    }

    public int deleteAll() {
        String hql = "delete from LoggingEventPropertyDBItem";
        Query query = setQueryParams(hql);
        int row = query.executeUpdate();
        return row;
    }

    public void delete(String mappedKey, String mappedValue, String loggerName) {
        List<LoggingEventPropertyDBItem> records = getListOfPropertyDBItems(mappedKey, mappedValue, loggerName);
        beginTransaction();
        for(LoggingEventPropertyDBItem item : records) {
            delete(item);
        }
        commit();
    }

}
