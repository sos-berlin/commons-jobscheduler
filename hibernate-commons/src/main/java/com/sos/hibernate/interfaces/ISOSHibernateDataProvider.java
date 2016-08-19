package com.sos.hibernate.interfaces;

import org.eclipse.swt.widgets.Table;

/** @author Uwe Risse */
public interface ISOSHibernateDataProvider {

    public void fillTable(Table table);

    public ISOSHibernateFilter getFilter();

    public void resetFilter();

    public void getData(int limit) throws Exception;

}