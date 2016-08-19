package com.sos.testframework.h2;

import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.layer.SOSHibernateDBLayer;

public class Table1DBLayer extends SOSHibernateDBLayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Table1DBLayer.class);
    private static final String FIELD1 = "name";

    public Table1DBLayer(String configurationFile) {
        super();
        this.setConfigurationFileName(configurationFile);
        initConnection(configurationFile);
    }

    private Query setQueryParams(Table1Filter filter, String hql) throws Exception {
        Query query = null;
        getConnection().beginTransaction();
        query = getConnection().createQuery(hql);
        if (filter.getName() != null) {
            query.setParameter(FIELD1, filter.getName());
        }
        return query;
    }

    private String getWhere(Table1Filter filter) {
        String where = "";
        String and = "";
        if (filter.getName() != null) {
            where += and + " name = ( :name )";
            and = " and ";
        }
        return (where.isEmpty()) ? where : "where " + where;
    }

    public Table1DBItem getByName(String name) throws Exception {
        Table1Filter filter = new Table1Filter();
        filter.setName(name);
        LOGGER.info("check name " + filter.getName());
        Query query = setQueryParams(filter, "from com.sos.testframework.h2.Table1DBItem table_1 " + getWhere(filter));
        List<Table1DBItem> resultList = query.list();
        Table1DBItem record = resultList.get(0);
        return record;
    }

    public long addRecord(String name) throws Exception {
        Table1DBItem record = new Table1DBItem();
        record.setName(name);
        this.getConnection().beginTransaction();
        this.getConnection().saveOrUpdate(record);
        this.getConnection().commit();
        return record.getId();
    }

}