package com.sos.dialog.interfaces;

import com.sos.hibernate.interfaces.ISOSDashboardDataProvider;

public interface ITableView {
    public abstract void getTableData();
	public abstract void buildTable();
	public abstract void createTable();
	public abstract void createMenue();
	public void getList();
    public void actualizeList();
    public ISOSDashboardDataProvider getTableDataProvider();
 
}
