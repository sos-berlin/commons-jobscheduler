package com.sos.eventhandlerservice.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.model.conditions.OutCondition;
import com.sos.joc.model.conditions.OutConditionEvent;

public class DBLayerOutConditionEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerOutConditionEvents.class);
    private static final String DBItemOutConditionEvent = DBItemOutConditionEvent.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerOutConditionEvents(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemOutConditionEvent getOutConditionEventdDbItem(final Long id) throws Exception {
        return (DBItemOutConditionEvent) sosHibernateSession.get(DBItemOutConditionEvent.class, id);
    }

    public FilterOutConditionEvents resetFilter() {
        FilterOutConditionEvents filter = new FilterOutConditionEvents();
        filter.setEvent("");
        return filter;
    }

    private String getWhere(FilterOutConditionEvents filter) {
        String where = "";
        String and = "";

        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            where += and + " event = :event";
            and = " and ";
        }

        if (filter.getCommand() != null && !"".equals(filter.getCommand())) {
            where += and + " command = :command";
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

    private <T> Query<T> bindParameters(FilterOutConditionEvents filter, Query<T> query) {
        if (filter.getCommand() != null && !"".equals(filter.getCommand())) {
            query.setParameter("command", filter.getCommand());
        }
        if (filter.getEvent() != null && !"".equals(filter.getEvent())) {
            query.setParameter("event", filter.getEvent());
        }
        if (filter.getOutConditionId() != null) {
            query.setParameter("outConditionId", filter.getOutConditionId());
        }

        return query;

    }

    public List<DBItemOutConditionEvent> getOutConditionEventsList(FilterOutConditionEvents filter, final int limit) throws SOSHibernateException {
        String q = " from " + DBItemOutConditionEvent + getWhere(filter);
        LOGGER.debug("OutConditionEvents sql: " + q);
        Query<DBItemOutConditionEvent> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public int delete(FilterOutConditionEvents filterConditionEvents) throws SOSHibernateException {
        String hql = "delete from " + DBItemOutConditionEvent + " i " + getWhere(filterConditionEvents);
        int row = 0;
        Query<DBItemOutConditionEvent> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filterConditionEvents, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void deleteInsert(DBItemOutCondition dbItemOutCondition, OutCondition outCondition) throws SOSHibernateException {
        FilterOutConditionEvents filterOutConditionEvents = new FilterOutConditionEvents();
        filterOutConditionEvents.setOutConditionId(outCondition.getId());
        delete(filterOutConditionEvents);
        for (OutConditionEvent outConditionEvent : outCondition.getOutconditionEvents()) {
            DBItemOutConditionEvent dbItemOutConditionEvent = new DBItemOutConditionEvent();
            dbItemOutConditionEvent.setOutConditionId(dbItemOutCondition.getId());
            if (outConditionEvent.getCommand().isEmpty()) {
                dbItemOutConditionEvent.setEvent(outConditionEvent.getEvent());
            } else {
                dbItemOutConditionEvent.setEvent(outConditionEvent.getCommand() + ":" + outConditionEvent.getEvent());
            }
            sosHibernateSession.save(dbItemOutConditionEvent);
        }
    }
}