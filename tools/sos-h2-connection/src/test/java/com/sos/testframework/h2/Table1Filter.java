package com.sos.testframework.h2;

import com.sos.hibernate.classes.DbItem;
import com.sos.hibernate.classes.SOSHibernateFilter;

public class Table1Filter extends SOSHibernateFilter implements com.sos.hibernate.interfaces.ISOSHibernateFilter {

	private String name = null;
	
	@Override
	public String getTitle() {
		return "";
	}

	@Override
	public boolean isFiltered(DbItem arg0) {
		return false;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

}
