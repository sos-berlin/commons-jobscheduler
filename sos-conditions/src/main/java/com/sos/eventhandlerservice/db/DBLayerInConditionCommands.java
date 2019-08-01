package com.sos.eventhandlerservice.db;

import java.util.List;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.classes.SearchStringHelper;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.joc.model.conditions.InCondition;
import com.sos.joc.model.conditions.InConditionCommand;
import com.sos.joc.model.conditions.InConditions;

public class DBLayerInConditionCommands {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerInConditionCommands.class);
    private static final String DBItemInConditionCommand = DBItemInConditionCommand.class.getSimpleName();
    private static final String DBItemInCondition = DBItemInCondition.class.getSimpleName();
    private final SOSHibernateSession sosHibernateSession;

    public DBLayerInConditionCommands(SOSHibernateSession session) {
        this.sosHibernateSession = session;
    }

    public DBItemInConditionCommand getInConditionCommandDbItem(final Long id) throws Exception {
        return (DBItemInConditionCommand) sosHibernateSession.get(DBItemInConditionCommand.class, id);
    }

    public FilterInConditionCommands resetFilter() {
        FilterInConditionCommands filter = new FilterInConditionCommands();
        filter.setCommand("");
        filter.setCommandParam("");
        return filter;
    }

    private String getDeleteWhere(FilterInConditionCommands filter) {
        String where = "c.inConditionId = i.id ";
        String and = " and ";


        if (filter.getJob() != null && !"".equals(filter.getJob())) {
            where += and + " i.job = :job";
        }

        where = " where " + where;
        return where;
    }    
    
    private String getWhere(FilterInConditionCommands filter) {
        String where = "";
        String and = "";

        if (filter.getCommand() != null && !"".equals(filter.getCommand())) {
            where += and + " command = :command";
            and = " and ";
        }

        if (filter.getCommandParam() != null && !"".equals(filter.getCommandParam())) {
            where += and + " commandParam = :commandParam";
            and = " and ";
        }

        if (filter.getInConditionId() != null) {
            where += and + " inConditionId = :inConditionId";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterInConditionCommands filter, Query<T> query) {
        if (filter.getCommand() != null && !"".equals(filter.getCommand())) {
            query.setParameter("command", filter.getCommand());
        }
        if (filter.getCommandParam() != null && !"".equals(filter.getCommandParam())) {
            query.setParameter("commandParam", filter.getCommandParam());
        }

        if (filter.getInConditionId() != null) {
            query.setParameter("inConditionId", filter.getInConditionId());
        }

        return query;

    }

    public List<DBItemInConditionCommand> getInConditionCommandsList(FilterInConditionCommands filter, final int limit) throws SOSHibernateException {
        String q = " from " + DBItemInConditionCommand + getWhere(filter);
        LOGGER.debug("InConditionCommands sql: " + q);
        Query<DBItemInConditionCommand> query = sosHibernateSession.createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return sosHibernateSession.getResultList(query);
    }

    public int delete(FilterInConditionCommands filterConditionCommands) throws SOSHibernateException {
        String hql = "delete from " + DBItemInConditionCommand + " i " + getWhere(filterConditionCommands);
        int row = 0;
        Query<DBItemInConditionCommand> query = sosHibernateSession.createQuery(hql);
        query = bindParameters(filterConditionCommands, query);

        row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public int deleteCommandWithInConditions(FilterInConditionCommands filterConditionCommands) throws SOSHibernateException {
        String select = "select i.id from " + DBItemInConditionCommand + " c, " + DBItemInCondition + " i " + getDeleteWhere(filterConditionCommands);

        String hql = "delete from " + DBItemInConditionCommand + " where inConditionId in ( " + select + ")";
        Query<DBItemInConditionCommand> query = sosHibernateSession.createQuery(hql);
        bindParameters(filterConditionCommands, query);
        int row = sosHibernateSession.executeUpdate(query);
        return row;
    }

    public void deleteInsert(DBItemInCondition dbItemInCondition, InCondition inCondition) throws SOSHibernateException {
        FilterInConditionCommands filterInConditionCommands = new FilterInConditionCommands();
        filterInConditionCommands.setInConditionId(inCondition.getId());
        delete(filterInConditionCommands);
        for (InConditionCommand inConditionCommand : inCondition.getInconditionCommands()) {
            DBItemInConditionCommand dbItemInConditionCommand = new DBItemInConditionCommand();
            dbItemInConditionCommand.setInConditionId(dbItemInCondition.getId());
            dbItemInConditionCommand.setCommand(inConditionCommand.getCommand());
            dbItemInConditionCommand.setCommandParam(inConditionCommand.getCommandParam());
            sosHibernateSession.save(dbItemInConditionCommand);
        }

    }

}