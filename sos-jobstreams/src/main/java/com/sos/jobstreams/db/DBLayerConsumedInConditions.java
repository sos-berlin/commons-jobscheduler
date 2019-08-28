package com.sos.jobstreams.db;

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
            where += and + " c.session = :session";
            and = " and ";
        }
        if (filter.getJobSchedulerId() != null && !"".equals(filter.getJobSchedulerId())) {
            where += and + " i.schedulerId = :schedulerId";
            and = " and ";
        }

        if (filter.getInConditionId() != null) {
            where += and + " c.inConditionId = :inConditionId";
            and = " and ";
        }

        where = " where 1=1 " + and + where;
        return where;
    }

    private String getDeleteWhere(FilterConsumedInConditions filter) {
        String where = "";
        String and = "";

        if (filter.getJobSchedulerId() != null && !"".equals(filter.getJobSchedulerId())) {
            where += and + " schedulerId = :schedulerId";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " jobStream = :jobStream";
            and = " and ";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " job = :job";
            and = " and ";
        }

        where = " where " + where;
        return where;
    }

    private <T> Query<T> bindParameters(FilterConsumedInConditions filter, Query<T> query) {
        if (filter.getJobSchedulerId() != null && !"".equals(filter.getJobSchedulerId())) {
            query.setParameter("schedulerId", filter.getJobSchedulerId());
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
        if (filter.getInConditionId() != null) {
            query.setParameter("inConditionId", filter.getInConditionId());
        }

        return query;
    }

    public List<DBItemConsumedInCondition> getConsumedInConditionsList(FilterConsumedInConditions filter, final int limit)
            throws SOSHibernateException {

        String q = "select c from " + DBItemInCondition + " i, " + DBItemConsumedInCondition + " c " + getWhere(filter) + " and i.id=c.inConditionId";
      
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
        Query<DBItemConsumedInCondition> query = sosHibernateSession.createQuery(q);
        query.setParameter("schedulerId", filter.getJobSchedulerId());
        query.setParameter("session", filter.getSession());
        query.setParameter("job", filter.getJob());

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public int deleteByInConditionId(FilterConsumedInConditions filterConsumedInConditions) throws SOSHibernateException {
        String hql = "delete from " + DBItemConsumedInCondition + " c " + getWhere(filterConsumedInConditions);
        Query<DBItemConsumedInCondition> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filterConsumedInConditions, query);

        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public int deleteConsumedInConditions(FilterConsumedInConditions filterConsumedInConditions) throws SOSHibernateException {
        filterConsumedInConditions.setSession("");
        String select = "select id from " +  DBItemInCondition + getDeleteWhere(
                filterConsumedInConditions);

        String hql = "delete from " + DBItemConsumedInCondition + " where inConditionId in ( " + select + ")";
        Query<DBItemConsumedInCondition> query = sosHibernateSession.createQuery(hql);
        bindParameters(filterConsumedInConditions, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }
    
       public int updateConsumedInCondition(Long oldId, Long newId) throws SOSHibernateException {
        String hql = "update " + DBItemConsumedInCondition + " set inConditionId=" + newId + " where inConditionId=:oldId";
        int row = 0;
        Query<DBItemConsumedInCondition> query = sosHibernateSession.createQuery(hql);
        query.setParameter("oldId", oldId);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void deleteInsert(DBItemConsumedInCondition dbItemConsumedInCondition) throws SOSHibernateException {
        FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
        filterConsumedInConditions.setSession(dbItemConsumedInCondition.getSession());
        filterConsumedInConditions.setInConditionId(dbItemConsumedInCondition.getInConditionId());
        deleteByInConditionId(filterConsumedInConditions);
        sosHibernateSession.save(dbItemConsumedInCondition);
    }

}