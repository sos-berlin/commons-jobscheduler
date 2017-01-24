package com.sos.auth.shiro.db;

import java.util.List;

import org.hibernate.query.Query;

import com.sos.hibernate.layer.SOSHibernateDBLayer;

/** @author Uwe Risse */
public class SOSUserDBLayer extends SOSHibernateDBLayer {

    private SOSUserFilter filter = null;

    public SOSUserDBLayer(String configurationFileName) throws Exception {
        super();
        this.setConfigurationFileName(configurationFileName);
        this.createStatefullConnection(this.getConfigurationFileName());
        resetFilter();
    }

    public void resetFilter() {
        filter = new SOSUserFilter();
        filter.setUserName("");
    }

    public int delete() throws Exception {
        String hql = "delete from SOSUserDBItem " + getWhere();
        Query query = null;
        int row = 0;
        connection.beginTransaction();
        query = connection.createQuery(hql);
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

    @SuppressWarnings("unchecked")
    public List<SOSUserDBItem> getSOSUserList(final int limit) throws Exception {
        List<SOSUserDBItem> sosUserList = null;
        connection.beginTransaction();
        Query query = connection.createQuery("from SOSUserDBItem " + getWhere() + filter.getOrderCriteria() + filter.getSortMode());
        if (filter.getUserName() != null && !filter.getUserName().equals("")) {
            query.setParameter("sosUserName", filter.getUserName());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        sosUserList = query.list();
        return sosUserList;
    }

    public void setFilter(SOSUserFilter filter) {
        this.filter = filter;
    }

    public SOSUserFilter getFilter() {
        return filter;
    }

}