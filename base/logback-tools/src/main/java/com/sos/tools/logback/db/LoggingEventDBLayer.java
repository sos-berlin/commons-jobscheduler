package com.sos.tools.logback.db;

import com.sos.hibernate.layer.SOSHibernateDBLayer;
import org.hibernate.Query;

import java.io.File;
import java.util.List;

public class LoggingEventDBLayer extends SOSHibernateDBLayer {

    private static final String EVENT_ID = "eventId";
    private static final String LOGGER_NAME = "loggerName";

    private LoggingEventFilter filter = null;

    public LoggingEventDBLayer(File configurationFile) {
        super();
        this.filter = new LoggingEventFilter();
        this.setConfigurationFile(configurationFile);
        this.filter.setOrderCriteria(EVENT_ID);
        initSession();
    }

    private Query setQueryParams(String hql) {
        Query query = session.createQuery(hql);

        if (filter.getEventId() != null) {
            query.setLong(EVENT_ID, filter.getEventId());
        }

        if (filter.getLoggerName() != null) {
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

        if (filter.getLoggerName() != null) {
            where += and + " loggerName = :loggerName";
            and = " and ";
        }

        return (where.isEmpty()) ? where : "where " + where;
    }

    public int deleteAll() {
        String hql = "delete from LoggingEventDBItem";
        Query query = setQueryParams(hql);
        int row = query.executeUpdate();
        return row;
    }

    public List<LoggingEventDBItem> getAll() {
        Query query = setQueryParams("from LoggingEventDBItem " + this.filter.getOrderCriteria() + this.filter.getSortMode());
        return query.list();
    }

}
