package com.sos.testframework.h2;

import com.sos.hibernate.layer.SOSHibernateDBLayer;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class Table2DBLayer extends SOSHibernateDBLayer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Table2DBLayer.class);
    private static final String FIELD1 = "name";

    public Table2DBLayer(File configurationFile) {
        super();
        this.setConfigurationFile(configurationFile);
        initSession();
    }

    private Query setQueryParams(Table2Filter filter, String hql) {
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

    private String getWhere(Table2Filter filter) {
        String where = "";
        String and = "";
        if (filter.getName() != null) {
            where += and + " name = ( :name )";
            and = " and ";
        }
        return (where.isEmpty()) ? where : "where " + where;
    }

    public Table2DBItem getByName(String name) {
        Table2Filter filter = new Table2Filter();
        filter.setName(name);
        LOGGER.info("check name " + filter.getName());
        Query query = setQueryParams(filter, "from com.sos.testframework.h2.Table2DBItem table_1 " + getWhere(filter));
        List<Table2DBItem> resultList = query.list();
        Table2DBItem record = resultList.get(0);
        return record;
    }

    public long addRecord(String name) {
        Table2DBItem record = new Table2DBItem();
        record.setName(name);
        saveOrUpdate(record);
        commit();
        return record.getId();
    }

}