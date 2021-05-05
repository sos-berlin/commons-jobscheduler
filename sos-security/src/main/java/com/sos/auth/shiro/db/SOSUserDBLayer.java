package com.sos.auth.shiro.db;

import java.util.List;
import org.hibernate.query.Query;

import com.sos.hibernate.classes.SOSHibernateSession;
import com.sos.hibernate.layer.SOSHibernateDBLayer;

public class SOSUserDBLayer extends SOSHibernateDBLayer {

    private SOSUserFilter filter = null;
    private final SOSHibernateSession sosHibernateSession;

    public SOSUserDBLayer(SOSHibernateSession session) {
        this.sosHibernateSession = session;
        resetFilter();
    }

    public void resetFilter() {
        filter = new SOSUserFilter();
        filter.setUserName("");
    }

    public int delete() throws Exception {
        String hql = "delete from SOSUserDBItem " + getWhere();
        Query<SOSUserDBItem> query = null;
        int row = 0;
        sosHibernateSession.beginTransaction();
        query = sosHibernateSession.createQuery(hql);
        if (filter.getUserName() != null && !filter.getUserName().equals("")) {
            query.setParameter("sosUserName", filter.getUserName());
        }
        row = query.executeUpdate();
        return row;
    }

    private String getWhere() {
        String where = "";
        String and = "";
        if (filter.getUserName() != null && !filter.getUserName().equals("")) {
            where += and + " sosUserName = :sosUserName";
            and = " and ";
        }
        if (!where.trim().equals("")) {
            where = "where " + where;
        }
        return where;
    }

     public List<SOSUserDBItem> getSOSUserList(final int limit) throws Exception {
        List<SOSUserDBItem> sosUserList = null;
        sosHibernateSession.beginTransaction();
        Query<SOSUserDBItem> query = sosHibernateSession.createQuery("from SOSUserDBItem " + getWhere() + filter.getOrderCriteria() + filter.getSortMode());
        if (filter.getUserName() != null && !filter.getUserName().equals("")) {
            query.setParameter("sosUserName", filter.getUserName());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        sosUserList = query.getResultList();
        return sosUserList;
    }

    public void setFilter(SOSUserFilter filter) {
        this.filter = filter;
    }

    public SOSUserFilter getFilter() {
        return filter;
    }

}