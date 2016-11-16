package com.sos.hibernate.interfaces;

import com.sos.hibernate.classes.DbItem;

 
public interface ISOSHibernateFilter {

    public boolean isFiltered(DbItem h);

    public String getTitle();

}
