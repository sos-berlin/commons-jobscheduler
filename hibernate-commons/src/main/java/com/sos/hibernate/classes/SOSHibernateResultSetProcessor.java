package com.sos.hibernate.classes;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.ScrollMode;
import org.hibernate.engine.QueryParameters;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Problem: mariadb driver cannot execute inner selects inside of a
 * ScrollableResultSets.
 * 
 * This class uses intentionally Statement instead of PreparedStatement. */
public class SOSHibernateResultSetProcessor implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.getLogger(SOSHibernateResultSetProcessor.class);

    private SOSHibernateConnection connection;
    private Statement statement;
    private ResultSet resultSet;

    private Class<?> entity;
    private HashMap<String, Method> entityGetMethods;
    private HashMap<String, Method> entitySetMethods;
    private String sqlStatement;

    public SOSHibernateResultSetProcessor(SOSHibernateConnection conn) {
        connection = conn;
    }

    public ResultSet createResultSet(Criteria criteria, ScrollMode scrollMode) throws Exception {
        return createResultSet(null, criteria, scrollMode, null);
    }

    public ResultSet createResultSet(Class<?> resultEntity, Criteria criteria, ScrollMode scrollMode) throws Exception {
        return createResultSet(resultEntity, criteria, scrollMode, null);
    }

    public ResultSet createResultSet(Criteria criteria, ScrollMode scrollMode, Optional<Integer> fetchSize) throws Exception {
        return createResultSet(null, criteria, scrollMode, fetchSize);
    }

    public ResultSet createResultSet(Class<?> resultEntity, Criteria criteria, ScrollMode scrollMode, Optional<Integer> fetchSize) throws Exception {
        String method = "createResultSet";

        try {
            logger.debug(String.format("%s", method));

            CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
            if (resultEntity == null) {
                entity = Class.forName(criteriaImpl.getEntityOrClassName());
            } else {
                entity = resultEntity;
            }

            SessionImplementor session = criteriaImpl.getSession();
            SessionFactoryImplementor factory = session.getFactory();
            CriteriaQueryTranslator translator = new CriteriaQueryTranslator(factory, criteriaImpl, criteriaImpl.getEntityOrClassName(), CriteriaQueryTranslator.ROOT_SQL_ALIAS);

            String[] implementors = factory.getImplementors(criteriaImpl.getEntityOrClassName());
            CriteriaJoinWalker walker = new CriteriaJoinWalker((OuterJoinLoadable) factory.getEntityPersister(implementors[0]), translator, factory, criteriaImpl, criteriaImpl.getEntityOrClassName(), session.getLoadQueryInfluencers());

            String sql = createSqlStatement(translator, walker.getSQLString());
            createMetadata(translator);

            resultSet = createResultSet(sql, scrollMode, criteria.isReadOnly(), fetchSize);
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()));
        }

        return resultSet;
    }

    public ResultSet createResultSet(String sql, ScrollMode scrollMode, boolean isReadOnly) throws Exception {

        return createResultSet(sql, scrollMode, isReadOnly, null);
    }

    public ResultSet createResultSet(String sql, ScrollMode scrollMode, boolean isReadOnly, Optional<Integer> fetchSize) throws Exception {
        String method = "createResultSet";

        sqlStatement = sql;
        logger.debug(String.format("%s: sqlStatement = %s, scrollMode = %s, isReadOnly = %s, fetchSize= %s", method, sqlStatement, scrollMode.toString(), isReadOnly, fetchSize));

        statement = connection.getJdbcConnection().createStatement(getResultSetType(scrollMode), getConcurrencyMode(isReadOnly));

        if (fetchSize.isPresent()) {
            // use default value if fetchSize != 0. for example Oracle = 10
            // accept negative values. for example MySQL Integer.MIN_VALUE
            // -2147483648
            if (fetchSize.get() != 0) {
                statement.setFetchSize(fetchSize.get());
            }
        }
        resultSet = statement.executeQuery(sqlStatement);

        logger.debug(String.format("%s: statement.getFetchSize = %s", method, statement.getFetchSize()));
        return resultSet;
    }

    private String createSqlStatement(CriteriaQueryTranslator translator, String hibernateSqlString) throws Exception {
        String where = translator.getWhereCondition();

        QueryParameters qp = translator.getQueryParameters();
        Type[] types = qp.getPositionalParameterTypes();
        Object[] values = qp.getPositionalParameterValues();

        for (int i = 0; i < values.length; i++) {
            int index = where.indexOf("?");
            if (index > 0) {
                String val = connection.quote(types[i], values[i]);
                where = where.replaceFirst("\\?", val);
            }
        }
        return hibernateSqlString.replace(translator.getWhereCondition(), where);
    }

    /** @TODO works only with field aliases in the Projection definition
     * 
     * @throws Exception */
    private void createMetadata(CriteriaQueryTranslator translator) throws Exception {
        String method = "createMetadata";
        if (translator.getRootCriteria().getProjection() == null) {
            throw new Exception(String.format("%s: translator.getRootCriteria().getProjection() is NULL. Please use the Projection in the criteria definition", method));
        }

        entityGetMethods = new HashMap<String, Method>();
        entitySetMethods = new HashMap<String, Method>();
        String[] properties = translator.getProjectedAliases();
        String[] propertiesColumnAliases = translator.getProjectedColumnAliases();
        for (int i = 0; i < properties.length; i++) {
            String property = properties[i];
            if (property == null) {
                throw new Exception(String.format("%s: property is NULL. Please use the field aliases in the Projection definition", method));
            }

            Method getter = new PropertyDescriptor(property, entity).getReadMethod();
            Method setter = new PropertyDescriptor(property, entity).getWriteMethod();

            entityGetMethods.put(propertiesColumnAliases[i], getter);
            entitySetMethods.put(propertiesColumnAliases[i], setter);
        }
    }

    private int getConcurrencyMode(boolean readOnly) {
        return readOnly ? ResultSet.CONCUR_READ_ONLY : ResultSet.CONCUR_UPDATABLE;
    }

    private int getResultSetType(ScrollMode scrollMode) throws Exception {
        String method = "getResultSetType";

        int type = 0;
        if (scrollMode.equals(ScrollMode.FORWARD_ONLY)) {
            type = ResultSet.TYPE_FORWARD_ONLY;
        } else if (scrollMode.equals(ScrollMode.SCROLL_INSENSITIVE)) {
            type = ResultSet.TYPE_SCROLL_INSENSITIVE;
        } else if (scrollMode.equals(ScrollMode.SCROLL_SENSITIVE)) {
            type = ResultSet.TYPE_SCROLL_SENSITIVE;
        } else {
            throw new Exception(String.format("%s: not supported scroll mode = %s", method, scrollMode.toString()));
        }
        return type;
    }

    public Object get() throws Exception {
        if (entity == null) {
            throw new Exception("entity is NULL");
        }
        if (entitySetMethods == null) {
            throw new Exception("entitySetMethods is NULL");
        }
        if (entityGetMethods == null) {
            throw new Exception("entityGetMethods is NULL");
        }

        Object bean = entity.newInstance();
        for (Map.Entry<String, Method> setters : entitySetMethods.entrySet()) {
            String field = setters.getKey();
            Method setter = setters.getValue();
            Method getter = entityGetMethods.get(field);
            // else if(getter.getReturnType().equals(Date.class)){
            String returnTypeName = getter.getReturnType().getSimpleName();

            if (returnTypeName.equalsIgnoreCase("Long")) {
                setter.invoke(bean, resultSet.getLong(field));
            } else if (returnTypeName.equals("Timestamp")) {
                setter.invoke(bean, resultSet.getTimestamp(field));
            } else if (returnTypeName.equals("Date")) {
                setter.invoke(bean, resultSet.getTimestamp(field));
            } else if (returnTypeName.equalsIgnoreCase("boolean")) {
                org.hibernate.annotations.Type t = getter.getAnnotation(org.hibernate.annotations.Type.class);
                boolean setted = false;
                if (t != null) {
                    if (t.type().equalsIgnoreCase("numeric_boolean")) {
                        long val = resultSet.getLong(field);
                        setter.invoke(bean, val == 0 ? new Boolean(false) : new Boolean(true));
                        setted = true;
                    }
                }
                if (!setted) {
                    setter.invoke(bean, resultSet.getBoolean(field));
                }
            } else {
                setter.invoke(bean, resultSet.getString(field));
            }
        }
        return bean;
    }

    private void dispose() {
        resultSet = null;
        statement = null;
        entity = null;
        entityGetMethods = null;
        entitySetMethods = null;
        sqlStatement = null;
    }

    public void close() {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception ex) {
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (Exception ex) {
            }
        }
        dispose();
    }

    public String getSqlStatement() {
        return sqlStatement;
    }

}
