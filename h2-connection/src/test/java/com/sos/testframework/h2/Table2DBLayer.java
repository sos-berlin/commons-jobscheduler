package com.sos.testframework.h2;

import java.util.List;

import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.layer.SOSHibernateDBLayer;

public class Table2DBLayer extends SOSHibernateDBLayer {

    private final static Logger logger = LoggerFactory.getLogger(Table2DBLayer.class);

    private final static String FIELD1 = "name";

    public Table2DBLayer(String configurationFile) {
        super();
        this.setConfigurationFileName(configurationFile);
        initConnection(configurationFile);
    }

    private Query setQueryParams(Table2Filter filter, String hql) {
        Query query = null;
        try {
            getConnection().beginTransaction();
            query = getConnection().createQuery(hql);
            if (filter.getName() != null)
                query.setParameter(FIELD1, filter.getName());
        } catch (Exception e) {
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
        logger.info("check name " + filter.getName());
        Query query = setQueryParams(filter, "from com.sos.testframework.h2.Table2DBItem table_1 " + getWhere(filter));

        List<Table2DBItem> resultList = query.list();
        Table2DBItem record = resultList.get(0);
        return record;
    }

    public long addRecord(String name) {
        Table2DBItem record = new Table2DBItem();
        record.setName(name);
        try {
            this.getConnection().connect();
            this.getConnection().beginTransaction();
            this.getConnection().saveOrUpdate(record);
            this.getConnection().commit();
        } catch (Exception e) {
            logger.error("Error occurred adding record: ", e);
        }
        return record.getId();
    }

}
