package com.sos.tools.logback.db;

import com.sos.hibernate.layer.SOSHibernateDBLayer;
import org.hibernate.Query;

import java.io.File;
import java.util.List;

public class LoggingEventExceptionDBLayer extends SOSHibernateDBLayer {

    private static final String EVENT_ID = "eventId";
    private static final String I = "i";
    private static final String TRACE_LINE = "traceLine";

    private LoggingEventExceptionFilter filter = null;

    public LoggingEventExceptionDBLayer(File configurationFile) {
        super();
        this.filter = new LoggingEventExceptionFilter();
        this.setConfigurationFile(configurationFile);
        this.filter.setOrderCriteria(EVENT_ID);
        initSession();
    }

    private Query setQueryParams(String hql) {
        Query query = session.createQuery(hql);

        if (filter.getEventId() != null) {
            query.setLong(EVENT_ID, filter.getEventId());
        }

        if (filter.getI() != null) {
            query.setParameter(I, filter.getI());
        }

        if (filter.getTraceLine() != null) {
            query.setParameter(TRACE_LINE, filter.getTraceLine());
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

        if (filter.getI() != null) {
            where += and + " i = :i";
            and = " and ";
        }

        if (filter.getTraceLine() != null) {
            where += and + " traceLine = :traceLine";
            and = " and ";
        }

        return (where.isEmpty()) ? where : "where " + where;
    }

    public int deleteAll() {
        String hql = "delete from LoggingEventExceptionDBItem";
        Query query = setQueryParams(hql);
        int row = query.executeUpdate();
        return row;
    }

    public List<LoggingEventDBItem> getAll() {
        Query query = setQueryParams("from LoggingEventExceptionDBItem " + this.filter.getOrderCriteria() + this.filter.getSortMode());
        return query.list();
    }

}
