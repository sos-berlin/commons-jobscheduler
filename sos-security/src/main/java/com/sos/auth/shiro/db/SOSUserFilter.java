package com.sos.auth.shiro.db;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateFilter;
import com.sos.hibernate.interfaces.ISOSHibernateFilter;

 
public class SOSUserFilter extends SOSHibernateFilter implements ISOSHibernateFilter {

    @SuppressWarnings("unused")
    private final String conClassName = "SOSUserFilter";
    private String userName;

    public SOSUserFilter() {

    }

    @Override
    public boolean isFiltered(DbItem h) {
        return false;
    }

    @Override
    public String getTitle() {
        return userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
