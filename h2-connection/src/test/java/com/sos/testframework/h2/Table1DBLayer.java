package com.sos.testframework.h2;

import com.sos.hibernate.layer.SOSHibernateDBLayer;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class Table1DBLayer extends SOSHibernateDBLayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Table1DBLayer.class);
    private static final String FIELD1 = "name";

    public Table1DBLayer(File configurationFile) {
        super();
        this.setConfigurationFile(configurationFile);
        initSession();
    }

    private Query setQueryParams(Table1Filter filter, String hql) {
        Query query = null;
        try {
            query = session.createQuery(hql);
            if (filter.getName() != null) {
                query.setParameter(FIELD1, filter.getName());
            }
        } catch (HibernateException e) {
            throw new RuntimeException("Error creating Query", e);
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

    public Table1DBItem getByName(String name) {
        Table1Filter filter = new Table1Filter();
        filter.setName(name);
        LOGGER.info("check name " + filter.getName());
        Query query = setQueryParams(filter, "from com.sos.testframework.h2.Table1DBItem table_1 " + getWhere(filter));
        List<Table1DBItem> resultList = query.list();
        Table1DBItem record = resultList.get(0);
        return record;
    }

    public long addRecord(String name) {
        Table1DBItem record = new Table1DBItem();
        record.setName(name);
        saveOrUpdate(record);
        commit();
        return record.getId();
    }

}