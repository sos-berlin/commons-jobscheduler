package com.sos.jobstreams.db;

import java.util.List;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;

public class DBLayerEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerEvents.class);
    private static final String DBItemEvents = DBItemEvent.class.getSimpleName();
    private static final String DBItemOutCondition = DBItemOutCondition.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerEvents(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemEvent getEventsDbItem(final Long id) throws SOSHibernateException {
        return (DBItemEvent) sosHibernateSession.get(DBItemEvent.class, id);
    }

    public FilterEvents resetFilter() {
        FilterEvents filter = new FilterEvents();
        filter.setEvent("");
        filter.setSession("");
        filter.setJobStream("");
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

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " jobStream = :jobStream";
            and = " and ";
        }

        if (filter.getOutConditionId() != null) {
            where += and + " outConditionId = :outConditionId";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    private String getDeleteWhere(FilterEvents filter) {
        String where = "";
        String and = "";

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " job = :job";
            and = " and ";
        }

        where = " where " + where;
        return where;
    }

    private <T> Query<T> bindParameters(FilterEvents filter, Query<T> query) {
        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            query.setParameter("event", filter.getEvent());
        }
        if (filter.getSession() != null && !"".equals(filter.getSession())) {
            query.setParameter("session", filter.getSession());
        }
        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            query.setParameter("jobStream", filter.getJobStream());
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            query.setParameter("job", filter.getJob());
        }
        if (filter.getOutConditionId() != null) {
            query.setParameter("outConditionId", filter.getOutConditionId());
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
        filter.setSession(itemEvent.getSession());
        filter.setJobStream(itemEvent.getJobStream());
        delete(filter);
        sosHibernateSession.save(itemEvent);
    }

    public int deleteEventsWithOutConditions(FilterEvents filterEvents) throws SOSHibernateException {
        filterEvents.setSession("");
        String select = "select id from " + DBItemEvents + getDeleteWhere(filterEvents);
        String hql = "delete from " + DBItemEvents + " where outConditionId in ( " + select + ")";
        Query<DBItemOutConditionEvent> query = sosHibernateSession.createQuery(hql);
        bindParameters(filterEvents, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public int deleteEventsFromJobStream(FilterEvents filter) throws SOSHibernateException {
        filter.setSession("");

        String select = "select id from " + DBItemOutCondition
                + "  where jobStream=:jobStream";
        String hql = "delete from " + DBItemEvents + " where outConditionId in ( " + select + ")";
        Query<DBItemOutConditionEvent> query = sosHibernateSession.createQuery(hql);
        query.setParameter("jobStream", filter.getJobStream());
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public int updateEvents(Long oldId, Long newId) throws SOSHibernateException {
        String hql = "update " + DBItemEvents + " set outConditionId=" + newId + " where outConditionId=:oldId";
        int row = 0;
        Query<DBItemEvent> query = sosHibernateSession.createQuery(hql);
        query.setParameter("oldId", oldId);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

}