package com.sos.jobstreams.db;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.model.jobstreams.InCondition;
import com.sos.joc.model.jobstreams.InConditions;
import com.sos.joc.model.jobstreams.JobInCondition;

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
		filter.setJobSchedulerId("");
		filter.setJob("");
		filter.setJobStream("");
		return filter;
	}

	private String getWhere(FilterInConditions filter) {
		String where = "";
		String and = "";

		if (filter.getJobSchedulerId() != null && !"".equals(filter.getJobSchedulerId())) {
			where += and + " i.schedulerId = :schedulerId";
			and = " and ";
		}

		if (filter.getJob() != null && !"".equals(filter.getJob())) {
			where += and + " i.job = :job";
			and = " and ";
		}

		if (filter.getJobStream() != null && !"".equals(filter.getJobStream())) {
			where += and + " i.jobStream = :jobStream";
			and = " and ";
		}

		where = "where 1=1 " + and + where;
		return where;
	}

	private <T> Query<T> bindParameters(FilterInConditions filter, Query<T> query) {
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

	public List<DBItemInConditionWithCommand> getInConditionsList(FilterInConditions filter, final int limit)
			throws SOSHibernateException {
		String q = "select new com.sos.jobstreams.db.DBItemInConditionWithCommand(i,c) from "
				+ DBItemInCondition + " i, " + DBItemInConditionCommand + " c " + getWhere(filter)
				+ " and i.id=c.inConditionId";
		LOGGER.debug("InConditions sql: " + q);
		Query<DBItemInConditionWithCommand> query = sosHibernateSession.createQuery(q);
		query = bindParameters(filter, query);

		if (limit > 0) {
			query.setMaxResults(limit);
		}
		return sosHibernateSession.getResultList(query);
	}

	public List<DBItemInCondition> getSimpleInConditionsList(FilterInConditions filter, final int limit)
			throws SOSHibernateException {
		String q = "  from " + DBItemInCondition + " i  " + getWhere(filter);
		LOGGER.debug("InConditions sql: " + q);
		Query<DBItemInCondition> query = sosHibernateSession.createQuery(q);
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
        DBLayerConsumedInConditions dbLayerConsumedInConditions = new DBLayerConsumedInConditions(sosHibernateSession);
		for (JobInCondition jobInCondition : inConditions.getJobsInconditions()) {
	        DBLayerInConditions dbLayerInConditions = new DBLayerInConditions(sosHibernateSession);
	        FilterInConditions filterInConditions = new FilterInConditions();
	        filterInConditions.setJob(jobInCondition.getJob());
	        filterInConditions.setJobSchedulerId(inConditions.getJobschedulerId());
	        List<DBItemInCondition> listOfInCondititinos = dbLayerInConditions.getSimpleInConditionsList(filterInConditions, 0);
	        
	        delete(filterInConditions);

			for (InCondition inCondition : jobInCondition.getInconditions()) {
				DBItemInCondition dbItemInCondition = new DBItemInCondition();
				dbItemInCondition.setExpression(inCondition.getConditionExpression().getExpression());
				dbItemInCondition.setJob(jobInCondition.getJob());
				dbItemInCondition.setSchedulerId(inConditions.getJobschedulerId());
				dbItemInCondition.setJobStream(Paths.get(inCondition.getJobStream()).getFileName().toString());
				dbItemInCondition.setMarkExpression(inCondition.getMarkExpression());
				dbItemInCondition.setCreated(new Date());
				sosHibernateSession.save(dbItemInCondition);

				dbLayerInConditionCommands.deleteInsert(dbItemInCondition, inCondition);
			}
			
			for (DBItemInCondition dbItemInCondition: listOfInCondititinos) {
			    FilterInConditionCommands filterInConditionCommands = new FilterInConditionCommands();
		        filterInConditionCommands.setInConditionId(dbItemInCondition.getId());
		        dbLayerInConditionCommands.deleteByInConditionId(filterInConditionCommands);	
		        
		        FilterConsumedInConditions filterConsumedInConditions = new FilterConsumedInConditions();
		        filterConsumedInConditions.setInConditionId(dbItemInCondition.getId());
		        dbLayerConsumedInConditions.deleteByInConditionId(filterConsumedInConditions);
		     }
			
		}
	}

}