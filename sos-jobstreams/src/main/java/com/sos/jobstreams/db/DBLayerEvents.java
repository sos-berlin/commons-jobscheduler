package com.sos.jobstreams.db;

import java.util.List;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jobstreams.resolver.JSEvent;

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
        String where = "1=1";
        String and = " and ";

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " o.schedulerId = :schedulerId";
            and = " and ";
        }

        if (filter.getSession() != null && !"".equals(filter.getSession())) {
            where += and + " e.session = :session";
            and = " and ";
        }

        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            where += and + " e.event = :event";
            and = " and ";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " o.job = :job";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " e.jobStream = :jobStream";
            and = " and ";
        }

        if (filter.getOutConditionId() != null) {
            where += and + " e.outConditionId = :outConditionId";
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

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " schedulerId = :schedulerId";
            and = " and ";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " job = :job";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " jobStream = :jobStream";
            and = " and ";
        }

        where = " where " + where;
        return where;
    }

    private <T> Query<T> bindParameters(FilterEvents filter, Query<T> query) {
        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            query.setParameter("event", filter.getEvent());
        }
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
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
        String q = "select e from " + DBItemOutCondition + " o, " + DBItemEvents + " e " + getWhere(filter) + " and o.id=e.outConditionId";

        Query<DBItemEvent> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public Integer delete(FilterEvents filter) throws SOSHibernateException {

        String schedulerId = filter.getSchedulerId();
        filter.setSchedulerId("");
        String select = "select id from " + DBItemOutCondition + " where schedulerId = :schedulerId";
        String hql = "delete from " + DBItemEvents + " e " + getWhere(filter) + " and e.outConditionId in ( " + select + ")";

        int row = 0;
        Query<DBItemEvent> query = sosHibernateSession.createQuery(hql);
        filter.setSchedulerId(schedulerId);
        query = bindParameters(filter, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;

    }

    public void store(JSEvent event) throws SOSHibernateException {
        FilterEvents filterEvents = new FilterEvents();
        filterEvents.setEvent(event.getEvent());
        filterEvents.setJobStream(event.getJobStream());
        filterEvents.setOutConditionId(event.getOutConditionId());
        filterEvents.setSession(event.getSession());
        filterEvents.setSchedulerId(event.getSchedulerId());
        delete(filterEvents);
        sosHibernateSession.save(event.getItemEvent());
    }

    public int deleteEventsWithOutConditions(FilterEvents filterEvents) throws SOSHibernateException {
        filterEvents.setSession("");
        String select = "select id from " + DBItemOutCondition + getDeleteWhere(filterEvents);
        String hql = "delete from " + DBItemEvents + " where outConditionId in ( " + select + ")";
        Query<DBItemOutConditionEvent> query = sosHibernateSession.createQuery(hql);
        bindParameters(filterEvents, query);
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