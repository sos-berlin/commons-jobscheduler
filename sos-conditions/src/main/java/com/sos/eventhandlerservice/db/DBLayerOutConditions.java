package com.sos.eventhandlerservice.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.model.conditions.InCondition;
import com.sos.joc.model.conditions.InConditions;
import com.sos.joc.model.conditions.OutCondition;
import com.sos.joc.model.conditions.OutConditions;

public class DBLayerOutConditions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerOutConditions.class);
    private static final String DBItemOutCondition = DBItemOutCondition.class.getSimpleName();
    private static final String DBItemOutConditionEvent = DBItemOutConditionEvent.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerOutConditions(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemInCondition getConditionsDbItem(final Long id) throws Exception {
        return (DBItemInCondition) sosHibernateSession.get(DBItemInCondition.class, id);
    }

    public FilterOutConditions resetFilter() {
        FilterOutConditions filter = new FilterOutConditions();
        filter.setMasterId("");
        filter.setJob("");
        return filter;
    }

    private String getWhere(FilterOutConditions filter) {
        String where = "";
        String and = "";

        if (filter.getMasterId() != null && !"".equals(filter.getMasterId())) {
            where += and + " o.masterId = :masterId";
            and = " and ";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " o.job = :job";
            and = " and ";
        }

        where = "where 1=1 " + and + where;
        return where;
    }

    private <T> Query<T> bindParameters(FilterOutConditions filter, Query<T> query) {
        if (filter.getMasterId() != null && !"".equals(filter.getMasterId())) {
            query.setParameter("masterId", filter.getMasterId());
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            query.setParameter("job", filter.getJob());
        }

        return query;
    }

    public List<DBItemOutConditionWithEvent> getOutConditionsList(FilterOutConditions filter, final int limit) throws SOSHibernateException {
        String q = "select new com.sos.eventhandlerservice.db.DBItemOutConditionWithEvent(o,e) from " + DBItemOutCondition + " o, "
                + DBItemOutConditionEvent + " e " + getWhere(filter) + " and o.id=e.outConditionId";
        LOGGER.debug("OutConditions sql: " + q);
        Query<DBItemOutConditionWithEvent> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }
    
    public int delete(FilterOutConditions filterOutConditions) throws SOSHibernateException {
        String hql = "delete from " + DBItemOutCondition + " o " + getWhere(filterOutConditions);
        int row = 0;
        Query<DBItemOutCondition> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filterOutConditions, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void deleteInsert(OutConditions outConditions) throws SOSHibernateException {
        DBLayerOutConditionEvents dbLayerOutConditionEvents = new DBLayerOutConditionEvents(sosHibernateSession);
        FilterOutConditions filterOutConditions = new FilterOutConditions();
        filterOutConditions.setJob(outConditions.getJob());
        filterOutConditions.setMasterId(outConditions.getMasterId());
        delete(filterOutConditions);
        for (OutCondition outCondition : outConditions.getOutconditions()) {
            DBItemOutCondition dbItemOutCondition = new DBItemOutCondition();
            dbItemOutCondition.setExpression(outCondition.getConditionExpression().getExpression());
            dbItemOutCondition.setJob(outConditions.getJob());
            dbItemOutCondition.setMasterId(outConditions.getMasterId());
            sosHibernateSession.save(dbItemOutCondition);

            dbLayerOutConditionEvents.deleteInsert(dbItemOutCondition, outCondition);
        }
    }

}