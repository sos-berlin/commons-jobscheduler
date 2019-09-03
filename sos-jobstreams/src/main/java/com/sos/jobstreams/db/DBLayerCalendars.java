package com.sos.jobstreams.db;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.jitl.reporting.db.DBItemInventoryClusterCalendar;
import com.sos.jitl.reporting.db.DBLayer;

public class DBLayerCalendars extends DBLayer {

    public DBLayerCalendars(SOSHibernateSession sosHibernateSession) {
        super(sosHibernateSession);
    }

    private String getWhere(FilterCalendarUsage filter) {
        String where = "1=1";
        String and = " and ";

        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            where += and + " c.schedulerId = :schedulerId";
            and = " and ";
        }

        if (filter.getPath() != null && !"".equals(filter.getPath())) {
            where += and + " u.path = :path";
            and = " and ";
        }

        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    private <T> Query<T> bindParameters(FilterCalendarUsage filter, Query<T> query) {
        if (filter.getSchedulerId() != null && !"".equals(filter.getSchedulerId())) {
            query.setParameter("schedulerId", filter.getSchedulerId());
        }
        if (filter.getPath() != null && !"".equals(filter.getPath())) {
            query.setParameter("path", filter.getPath());
        }
        return query;
    }

    public List<DBItemInventoryClusterCalendar> getCalendar(FilterCalendarUsage filter, final int limit) throws SOSHibernateException {
        String q = "select c from " + DBITEM_INVENTORY_CLUSTER_CALENDAR_USAGE + " u " + DBITEM_CLUSTER_CALENDARS + " c " + getWhere(filter)
                + " c.id=u.calendarId ";
        Query<DBItemInventoryClusterCalendar> query = super.getSession().createQuery(q);
        query = bindParameters(filter, query);

        if (limit > 0) {
            query.setMaxResults(limit);
        }
        return super.getSession().getResultList(query);
    }

}
