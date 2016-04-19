package com.sos.auth.shiro.db;

import java.io.File;
import java.util.List;

import org.hibernate.Query;

import com.sos.hibernate.layer.SOSHibernateDBLayer;

/** @author Uwe Risse */
public class SOSUserDBLayer extends SOSHibernateDBLayer {

    private SOSUserFilter filter = null;

    public SOSUserDBLayer(final File configurationFile_) {
        super();
        this.setConfigurationFile(configurationFile_);
        resetFilter();
    }

    public void resetFilter() {
        filter = new SOSUserFilter();
        filter.setUserName("");
    }

    public int delete() {
        if (session == null) {
            beginTransaction();
        }
        String hql = "delete from SOSUserDBItem " + getWhere();
        Query query = session.createQuery(hql);
        if (filter.getUserName() != null && !"".equals(filter.getUserName())) {
            query.setParameter("sosUserName", filter.getUserName());
        }
        int row = query.executeUpdate();
        return row;
    }

    private String getWhere() {
        String where = "";
        String and = "";
        if (filter.getUserName() != null && !"".equals(filter.getUserName())) {
            where += and + " sosUserName = :sosUserName";
            and = " and ";
        }
        if (!"".equals(where.trim())) {
            where = "where " + where;
        }
        return where;
    }

    public List<SOSUserDBItem> getSOSUserList(final int limit) {
        initSession();
        Query query = session.createQuery("from SOSUserDBItem " + getWhere() + filter.getOrderCriteria() + filter.getSortMode());
        if (filter.getUserName() != null && !"".equals(filter.getUserName())) {
            query.setParameter("sosUserName", filter.getUserName());
        }
        if (limit > 0) {
            query.setMaxResults(limit);
        }
        @SuppressWarnings("unchecked")
        List<SOSUserDBItem> sosUserList = query.list();
        return sosUserList;
    }

    public void setFilter(SOSUserFilter filter) {
        this.filter = filter;
    }

    public SOSUserFilter getFilter() {
        return filter;
    }

}