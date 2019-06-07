package com.sos.eventhandlerservice.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.model.conditions.InCondition;
import com.sos.joc.model.conditions.InConditions;
import com.sos.joc.model.conditions.JobInCondition;
import com.sos.joc.model.job.JobPath;

public class DBLayerInConditions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerInConditions.class);
    private static final String DBItemInCondition = DBItemInCondition.class.getSimpleName();
    private static final String DBItemInConditionCommand = DBItemInConditionCommand.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerInConditions(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemInCondition getConditionsDbItem(final Long id) throws Exception {
        return (DBItemInCondition) sosHibernateSession.get(DBItemInCondition.class, id);
    }

    public FilterInConditions resetFilter() {
        FilterInConditions filter = new FilterInConditions();
        filter.setMasterId("");
        filter.setJob("");
        filter.setWorkflow("");
        return filter;
    }

    private String getWhere(FilterInConditions filter) {
        String where = "";
        String and = "";

        if (filter.getMasterId() != null && !"".equals(filter.getMasterId())) {
            where += and + " i.masterId = :masterId";
            and = " and ";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " i.job = :job";
            and = " and ";
        }

        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            where += and + " i.workflow = :workflow";
            and = " and ";
        }

        where = "where 1=1 " + and + where;
        return where;
    }

    private <T> Query<T> bindParameters(FilterInConditions filter, Query<T> query) {
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

    public List<DBItemInConditionWithCommand> getInConditionsList(FilterInConditions filter, final int limit) throws SOSHibernateException {
        String q = "select new com.sos.eventhandlerservice.db.DBItemInConditionWithCommand(i,c) from " + DBItemInCondition + " i, "
                + DBItemInConditionCommand + " c " + getWhere(filter) + " and i.id=c.inConditionId";
        LOGGER.debug("InConditions sql: " + q);
        Query<DBItemInConditionWithCommand> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public int delete(FilterInConditions filterInConditions) throws SOSHibernateException {
        String hql = "delete from " + DBItemInCondition + " i " + getWhere(filterInConditions);
        int row = 0;
        Query<DBItemInCondition> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filterInConditions, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void deleteInsert(InConditions inConditions) throws SOSHibernateException {
        DBLayerInConditionCommands dbLayerInConditionCommands = new DBLayerInConditionCommands(sosHibernateSession);
        for (JobInCondition jobInCondition : inConditions.getJobsInconditions()) {
            FilterInConditions filterInConditions = new FilterInConditions();
            filterInConditions.setJob(jobInCondition.getJob());
            filterInConditions.setMasterId(inConditions.getMasterId());
            delete(filterInConditions);

            for (InCondition inCondition : jobInCondition.getInconditions()) {
                DBItemInCondition dbItemInCondition = new DBItemInCondition();
                dbItemInCondition.setExpression(inCondition.getConditionExpression().getExpression());
                dbItemInCondition.setJob(jobInCondition.getJob());
                dbItemInCondition.setMasterId(inConditions.getMasterId());
                dbItemInCondition.setWorkflow(inCondition.getWorkflow());
                sosHibernateSession.save(dbItemInCondition);

                dbLayerInConditionCommands.deleteInsert(dbItemInCondition, inCondition);
            }
        }
    }

}