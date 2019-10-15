package com.sos.jobstreams.db;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.jobstreams.db.DBItemOutCondition;
import com.sos.jitl.jobstreams.db.DBItemOutConditionEvent;
import com.sos.jitl.jobstreams.db.DBItemOutConditionWithConfiguredEvent;
import com.sos.jobstreams.resolver.JSEventKey;
import com.sos.joc.model.jobstreams.JobOutCondition;
import com.sos.joc.model.jobstreams.OutCondition;
import com.sos.joc.model.jobstreams.OutConditions;

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
        filter.setJobSchedulerId("");
        filter.setJob("");
        filter.setJobStream("");
        return filter;
    }

    public String getEventListSql(Set<JSEventKey> list) {
        StringBuilder sql = new StringBuilder();

        for (JSEventKey s : list) {
            if (s.getJobStream().isEmpty()) {
                sql.append("e.event = " + "'" + s.getEvent() + "'").append(" or ");
            } else {
                sql.append("(e.event = " + "'" + s.getEvent() + "'").append(" and ").append("o.jobStream = " + "'" + s.getJobStream() + "')").append(
                        " or ");
            }
        }
        sql.append("1=0");

        return " (" + sql.toString() + ") ";
    }

    private String getWhere(FilterOutConditions filter) {
        String where = "";
        String and = "";

        if (filter.getJobSchedulerId() != null && !"".equals(filter.getJobSchedulerId())) {
            where += and + " o.schedulerId = :schedulerId";
            and = " and ";
        }

        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " o.job = :job";
            and = " and ";
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            where += and + " o.jobStream = :jobStream";
            and = " and ";
        }

        if (filter.getListOfEvents() != null && filter.getListOfEvents().size() > 0) {
            where += and + getEventListSql(filter.getListOfEvents());
        }

        where = "where 1=1 " + and + where;
        return where;
    }

    private <T> Query<T> bindParameters(FilterOutConditions filter, Query<T> query) {
        if (filter.getJobSchedulerId() != null && !"".equals(filter.getJobSchedulerId())) {
            query.setParameter("schedulerId", filter.getJobSchedulerId());
        }
        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            query.setParameter("job", filter.getJob());
        }

        if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
            query.setParameter("jobStream", filter.getJobStream());
        }

        return query;
    }

    public List<DBItemOutConditionWithConfiguredEvent> getOutConditionsList(FilterOutConditions filter, final int limit) throws SOSHibernateException {
        String q = "select new com.sos.jitl.jobstreams.db.DBItemOutConditionWithConfiguredEvent(o,e) from " + DBItemOutCondition + " o, " + DBItemOutConditionEvent
                + " e " + getWhere(filter) + " and o.id=e.outConditionId";
        Query<DBItemOutConditionWithConfiguredEvent> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public List<DBItemOutCondition> getSimpleOutConditionsList(FilterOutConditions filter, final int limit) throws SOSHibernateException {
        String q = " from " + DBItemOutCondition + " o " + getWhere(filter);
        Query<DBItemOutCondition> query = sosHibernateSession.createQuery(q);
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
        DBLayerOutConditions dbLayerOutConditions = new DBLayerOutConditions(sosHibernateSession);
        DBLayerOutConditionEvents dbLayerOutConditionEvents = new DBLayerOutConditionEvents(sosHibernateSession);
        DBLayerEvents dbLayerEvents = new DBLayerEvents(sosHibernateSession);
        for (JobOutCondition jobOutCondition : outConditions.getJobsOutconditions()) {

            FilterOutConditions filterOutConditions = new FilterOutConditions();
            filterOutConditions.setJob(jobOutCondition.getJob());
            filterOutConditions.setJobSchedulerId(outConditions.getJobschedulerId());

            List<DBItemOutCondition> listOfOutConditions = dbLayerOutConditions.getSimpleOutConditionsList(filterOutConditions, 0);
            delete(filterOutConditions);

            for (OutCondition outCondition : jobOutCondition.getOutconditions()) {
                Long oldId = outCondition.getId();

                DBItemOutCondition dbItemOutCondition = new DBItemOutCondition();
                String expression = outCondition.getConditionExpression().getExpression();
                if (expression == null || expression.isEmpty()) {
                    expression = "false";
                }
                dbItemOutCondition.setExpression(expression);
                dbItemOutCondition.setJob(jobOutCondition.getJob());
                dbItemOutCondition.setSchedulerId(outConditions.getJobschedulerId());
                dbItemOutCondition.setJobStream(outCondition.getJobStream());
                dbItemOutCondition.setCreated(new Date());
                sosHibernateSession.save(dbItemOutCondition);
                dbLayerOutConditionEvents.deleteInsert(dbItemOutCondition, outCondition);
                Long newId = dbItemOutCondition.getId();
                if (oldId != null) {
                    dbLayerEvents.updateEvents(oldId, newId);
                }
            }
            for (DBItemOutCondition dbItemOutCondition : listOfOutConditions) {
                FilterOutConditionEvents filterOutConditionEvents = new FilterOutConditionEvents();
                filterOutConditionEvents.setOutConditionId(dbItemOutCondition.getId());
                dbLayerOutConditionEvents.delete(filterOutConditionEvents);

                FilterEvents filterEvents = new FilterEvents();
                filterEvents.setOutConditionId(dbItemOutCondition.getId());
                dbLayerEvents.delete(filterEvents);
            }
        }

    }

}