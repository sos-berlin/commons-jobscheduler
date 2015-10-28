package com.sos.tools.logback.db;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;

import com.sos.hibernate.layer.SOSHibernateDBLayer;

public class LoggingEventDBLayer extends SOSHibernateDBLayer {

    private static final String EVENT_ID = "eventId";
    private static final String LOGGER_NAME = "loggerName";
    private Logger logger = Logger.getLogger(LoggingEventDBLayer.class);

    private LoggingEventFilter filter = null;

    public LoggingEventDBLayer(String configurationFileName) {
        super();
        this.filter = new LoggingEventFilter();
        this.setConfigurationFileName(configurationFileName);
        this.initConnection(this.getConfigurationFileName());
        this.filter.setOrderCriteria(EVENT_ID);
    }

    private Query setQueryParams(String hql) {
        Query query = null;
		try {
			connection.connect();
			connection.beginTransaction();
			query = connection.createQuery(hql);
			if (filter.getEventId() != null) {
			    query.setLong(EVENT_ID, filter.getEventId());
			}
			if (filter.getLoggerName() != null) {
			    query.setParameter(LOGGER_NAME, filter.getLoggerName());
			}
		} catch (Exception e) {
			logger.error("Error occurred creating query: ", e);
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
