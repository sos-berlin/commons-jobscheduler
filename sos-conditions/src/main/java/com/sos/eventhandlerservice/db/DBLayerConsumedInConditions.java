package com.sos.eventhandlerservice.db;

import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;

public class DBLayerConsumedInConditions {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerConsumedInConditions.class);
    private static final String DBItemConsumedInCondition = DBItemConsumedInCondition.class.getSimpleName();
    private static final String DBItemInCondition = DBItemInCondition.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerConsumedInConditions(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemConsumedInCondition getConsumedConditionsDbItem(final Long id) throws Exception {
        return (DBItemConsumedInCondition) sosHibernateSession.get(DBItemConsumedInCondition.class, id);
    }

    public FilterConsumedInConditions resetFilter() {
        FilterConsumedInConditions filter = new FilterConsumedInConditions();
        filter.setSession("");
        return filter;
    }

    private String getWhere(FilterConsumedInConditions filter) {
        String where = "";
        String and = "";

        if (filter.getSession() != null && !"".equals(filter.getSession())) {
            where += and + " session = :session";
            and = " and ";
        }

        if (filter.getInConditionId() != null) {
            where += and + " inConditionId = :inConditionId";
            and = " and ";
        }

        where = " where 1=1 " + and + where;
        return where;
    }

    private String getDeleteWhere(FilterConsumedInConditions filter) {
        String where = "c.inConditionId = i.id ";
        String and = " and ";

        if (filter.getSession() != null && !"".equals(filter.getSession())) {
            where += and + " c.session = :session";
        }

        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            where += and + " i.workflow = :workflow";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " i.job = :job";
        }

        where = " where " + where;
        return where;
    }

    private <T> Query<T> bindParameters(FilterConsumedInConditions filter, Query<T> query) {
        if (filter.getSession() != null && !"".equals(filter.getSession())) {
            query.setParameter("session", filter.getSession());
        }
        if (filter.getWorkflow() != null && !"".equals(filter.getWorkflow())) {
            query.setParameter("workflow", filter.getWorkflow());
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            query.setParameter("job", filter.getJob());
        }
        if (filter.getInConditionId() != null) {
            query.setParameter("inConditionId", filter.getInConditionId());
        }

        return query;
    }

    public List<DBItemConsumedInCondition> getConsumedInConditionsList(FilterConsumedInConditions filter, final int limit)
            throws SOSHibernateException {
        String q = " from " + DBItemConsumedInCondition + getWhere(filter);
        LOGGER.debug("ConsumedInConditions sql: " + q);
        Query<DBItemConsumedInCondition> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemConsumedInCondition> getConsumedInConditionsListByJob(FilterConsumedInConditions filter, final int limit)
            throws SOSHibernateException {

        String q = "select c from " + DBItemConsumedInCondition + " c, " + DBItemInCondition
                + " i where i.schedulerId=:schedulerId and c.inConditionId = i.id and c.session=:session and i.job=:job";
        LOGGER.debug("ConsumedInConditions sql: " + q);
        Query<DBItemConsumedInCondition> query = sosHibernateSession.createQuery(q);
        query.setParameter("schedulerId", filter.getJobSchedulerId());
        query.setParameter("session", filter.getSession());
        query.setParameter("job", filter.getJob());

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    private int delete(FilterConsumedInConditions filterConsumedInConditions) throws SOSHibernateException {
        String hql = "delete from " + DBItemConsumedInCondition + getWhere(filterConsumedInConditions);
        Query<DBItemConsumedInCondition> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filterConsumedInConditions, query);

        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public int deleteConsumedInConditions(FilterConsumedInConditions filterConsumedInConditions) throws SOSHibernateException {
        String select = "select i.id from " + DBItemConsumedInCondition + " c, " + DBItemInCondition + " i " + getDeleteWhere(
                filterConsumedInConditions);

        String hql = "delete from " + DBItemConsumedInCondition + " where inConditionId in ( " + select + ")";
        Query<DBItemConsumedInCondition> query = sosHibernateSession.createQuery(hql);
        bindParameters(filterConsumedInConditions, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void deleteInsert(DBItemConsumedInCondition dbItemConsumedInCondition) throws SOSHibernateException {
        FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
        filterConsumedInConditions.setSession(dbItemConsumedInCondition.getSession());
        filterConsumedInConditions.setInConditionId(dbItemConsumedInCondition.getInConditionId());
        delete(filterConsumedInConditions);
        sosHibernateSession.save(dbItemConsumedInCondition);
    }

}