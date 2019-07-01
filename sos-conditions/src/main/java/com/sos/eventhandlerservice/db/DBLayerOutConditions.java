package com.sos.eventhandlerservice.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.model.conditions.JobOutCondition;
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

    public DBItemOutCondition getOutConditionsDbItem(final Long id) throws Exception {
        return (DBItemOutCondition) sosHibernateSession.get(DBItemOutCondition.class, id);
    }

    public FilterOutConditions resetFilter() {
        FilterOutConditions filter = new FilterOutConditions();
        filter.setMasterId("");
        filter.setJob("");
        filter.setWorkflow("");
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

        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            where += and + " o.workflow = :workflow";
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

        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            query.setParameter("workflow", filter.getWorkflow());
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
        DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
        for (JobOutCondition jobOutCondition : outConditions.getJobsOutconditions()) {

            FilterOutConditions filterOutConditions = new FilterOutConditions();
            filterOutConditions.setJob(jobOutCondition.getJob());
            filterOutConditions.setMasterId(outConditions.getMasterId());
            delete(filterOutConditions);
                        
            for (OutCondition outCondition : jobOutCondition.getOutconditions()) {
                Long oldId = outCondition.getId();
                
                DBItemOutCondition dbItemOutCondition = new DBItemOutCondition();
                dbItemOutCondition.setExpression(outCondition.getConditionExpression().getExpression());
                dbItemOutCondition.setJob(jobOutCondition.getJob());
                dbItemOutCondition.setMasterId(outConditions.getMasterId());
                dbItemOutCondition.setWorkflow(outCondition.getWorkflow());
                sosHibernateSession.save(dbItemOutCondition);
                Long newId = dbItemOutCondition.getId();
                dbLayerEvents.updateEvents(oldId,newId);
                dbLayerOutConditionEvents.deleteInsert(dbItemOutCondition, outCondition);
                

            }
        }
    }

}