package com.sos.eventhandlerservice.db;

import java.util.List;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;

public class DBLayerEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerEvents.class);
    private static final String DBItemEvents = DBItemEvent.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerEvents(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemEvent getEventsDbItem(final Long id) throws Exception {
        return (DBItemEvent) sosHibernateSession.get(DBItemEvent.class, id);
    }

    public FilterEvents resetFilter() {
        FilterEvents filter = new FilterEvents();
        filter.setEvent("");
        filter.setSession("");
        return filter;
    }

    private String getWhere(FilterEvents filter) {
        String where = "";
        String and = "";

        if (filter.getSession() != null && !"".equals(filter.getSession())) {
            where += and + " session = :session";
            and = " and ";
        }

        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            where += and + " event = :event";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterEvents filter, Query<T> query) {
        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            query.setParameter("event", filter.getEvent());
        }
        if (filter.getSession() != null && !"".equals(filter.getSession())) {
            query.setParameter("session", filter.getSession());
        }
        return query;
    }

    public List<DBItemEvent> getEventsList(FilterEvents filter, final int limit) throws SOSHibernateException {
        String q = " from " + DBItemEvents + " " + getWhere(filter);
        LOGGER.debug("Events sql: " + q);
        Query<DBItemEvent> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }
    
    public Integer delete(FilterEvents filter) throws SOSHibernateException {
        String hql = "delete from " + DBItemEvents + " " + getWhere(filter);
        int row = 0;
        Query<DBItemEvent> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
       
    }

    public void store(DBItemEvent itemEvent) throws SOSHibernateException {
        FilterEvents filter = new FilterEvents();
        filter.setEvent(itemEvent.getEvent());
        filter.setEvent(itemEvent.getSession());
        delete(filter);
        sosHibernateSession.save(itemEvent);
    }

}