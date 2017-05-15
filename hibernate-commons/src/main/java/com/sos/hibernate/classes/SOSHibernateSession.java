package com.sos.hibernate.classes;

import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.JDBCException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.SessionImpl;
import org.hibernate.internal.StatelessSessionImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.SOSDBException;
import com.sos.exception.SOSDBSessionException;
import com.sos.exception.SOSException;
import com.sos.exception.SOSJDBCException;
import com.sos.hibernate.exceptions.SOSHibernateConnectionException;
import com.sos.hibernate.exceptions.SOSHibernateCriteriaException;
import com.sos.hibernate.exceptions.SOSHibernateException;
import com.sos.hibernate.exceptions.SOSHibernateOpenSessionException;
import com.sos.hibernate.exceptions.SOSHibernateQueryException;
import com.sos.hibernate.exceptions.SOSHibernateSessionException;
import com.sos.hibernate.exceptions.SOSHibernateTransactionException;

import sos.util.SOSDate;

public class SOSHibernateSession implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateSession.class);
    private final SOSHibernateFactory factory;
    private Object currentSession;
    private boolean isStatelessSession = false;
    private boolean isGetCurrentSession = false;
    private FlushMode defaultHibernateFlushMode = FlushMode.ALWAYS;
    private String identifier;
    private SOSHibernateSQLExecutor sqlExecutor;
    public static final int LIMIT_IN_CLAUSE = 1000;

    /** use factory.openSession() or factory.openStatelessSession(); */
    protected SOSHibernateSession(SOSHibernateFactory hibernateFactory) {
        this.factory = hibernateFactory;
    }

    public void setIdentifier(String val) {
        identifier = val;
    }

    protected void setIsStatelessSession(boolean val) {
        isStatelessSession = val;
    }

    protected void setIsGetCurrentSession(boolean val) {
        isGetCurrentSession = val;
    }

    public void setHibernateFlushMode(FlushMode flushMode) {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.setHibernateFlushMode(flushMode);
        }
    }

    public void setCacheMode(CacheMode cacheMode) {
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.setCacheMode(cacheMode);
        }
    }

    public boolean isStatelessSession() {
        return isStatelessSession;
    }

    public boolean isGetCurrentSession() {
        return isGetCurrentSession;
    }

    public boolean isOpen() {
        if (isStatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            return session.isOpen();
        } else {
            Session session = (Session) currentSession;
            return session.isOpen();
        }
    }

    public boolean isConnected() {
        if (isStatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            return session.isConnected();
        } else {
            Session session = (Session) currentSession;
            return session.isConnected();
        }
    }

    public SOSHibernateFactory getFactory() {
        return factory;
    }

    public String getIdentifier() {
        return identifier;
    }

    public FlushMode getHibernateFlushMode() {
        if (!isStatelessSession) {
            Session session = (Session) currentSession;
            return session.getHibernateFlushMode();
        }
        return null;
    }

    public CacheMode getCacheMode() {
        if (!isStatelessSession) {
            Session session = (Session) currentSession;
            return session.getCacheMode();
        }
        return null;
    }

    public Object getCurrentSession() {
        return currentSession;
    }

    public Connection getConnection() throws SOSHibernateException {
        String method = getMethodName("getConnection");
        LOGGER.debug(String.format("%s", method));
        try {
            if (isStatelessSession) {
                StatelessSessionImpl sf = (StatelessSessionImpl) currentSession;
                return sf.connection();
            } else {
                SessionImpl sf = (SessionImpl) currentSession;
                return sf.connection();
            }
        } catch (Throwable e) {
            throw new SOSHibernateConnectionException(e);
        }
    }

    @Deprecated
    public static SOSJDBCException getException(JDBCException ex) {
        return getException(ex, ex);
    }

    @Deprecated
    private static SOSJDBCException getException(JDBCException ex, Throwable t) {
        return new SOSJDBCException(ex.getMessage(), ex.getSQLException(), t, ex.getSQL());
    }

    @Deprecated
    public static SOSException getException(SOSException ex) {
        return ex;
    }

    @Deprecated
    public static Throwable getException(Throwable t) {
        Throwable e = t;
        while (e != null) {
            if (e instanceof JDBCException) {
                return getException((JDBCException) e, t);
            }
            e = e.getCause();
        }
        return t;
    }

    @Deprecated
    public static SOSDBException getSOSDBException(Throwable t) {
        Throwable e = t;
        while (e != null) {
            if (e instanceof JDBCException) {
                return getException((JDBCException) e, t);
            }
            e = e.getCause();
        }
        return new SOSDBException(t);
    }

    @Deprecated
    public static SOSException getSOSDBException(SOSException ex) {
        return ex;
    }

    public String getLastSequenceValue(String sequenceName) throws SOSHibernateException {
        String stmt = factory.getSequenceLastValString(sequenceName);
        return stmt == null ? null : getNativeQuerySingleValue(stmt);
    }

    public SOSHibernateSQLExecutor getSQLExecutor() {
        if (sqlExecutor == null) {
            sqlExecutor = new SOSHibernateSQLExecutor(this);
        }
        return sqlExecutor;
    }

    protected void openSession() throws SOSHibernateException {
        String method = getMethodName("openSession");
        if (currentSession != null) {
            LOGGER.debug(String.format("%s: close currentSession", method));
            closeSession();
        }
        LOGGER.debug(String.format("%s: isStatelessSession = %s, isGetCurrentSession = %s", method, isStatelessSession, isGetCurrentSession));
        try {

            if (isStatelessSession) {
                currentSession = factory.getSessionFactory().openStatelessSession();
            } else {
                Session session = null;
                if (isGetCurrentSession) {
                    session = factory.getSessionFactory().getCurrentSession();
                } else {
                    session = factory.getSessionFactory().openSession();
                }
                if (defaultHibernateFlushMode != null) {
                    session.setHibernateFlushMode(defaultHibernateFlushMode);
                }
                currentSession = session;
            }
        } catch (Throwable e) {
            throw new SOSHibernateOpenSessionException(e);
        }
    }

    /** @deprecated
     * 
     *             use factory.openSession() or factory.openStatelessSession(); */
    @Deprecated
    public void connect() throws SOSHibernateException {
        String method = getMethodName("connect");
        openSession();
        String connFile = (factory.getConfigFile().isPresent()) ? factory.getConfigFile().get().toAbsolutePath().toString() : "without config file";
        int isolationLevel = getFactory().getTransactionIsolation();
        LOGGER.debug(String.format("%s: autocommit = %s, transaction isolation = %s, %s", method, getFactory().getAutoCommit(), SOSHibernateFactory
                .getTransactionIsolationName(isolationLevel), connFile));

    }

    public int executeUpdate(Query<?> q) throws SOSHibernateException {
        try {
            return q.executeUpdate();
        } catch (Throwable e) {
            throw new SOSHibernateQueryException(e);
        }
    }

    public void clearSession() throws SOSHibernateException {
        String method = getMethodName("clearSession");
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new SOSHibernateSessionException("session is NULL");
        }
        try {
            if (!isStatelessSession) {
                Session session = (Session) currentSession;
                session.clear();
            }
        } catch (Throwable e) {
            throw new SOSHibernateSessionException(e);
        }
    }

    public void sessionDoWork(Work work) throws SOSHibernateException {
        String method = getMethodName("sessionDoWork");
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new SOSHibernateSessionException("currentSession is NULL");
        }
        try {
            if (!isStatelessSession) {
                Session session = (Session) currentSession;
                session.doWork(work);
            }
        } catch (Throwable e) {
            throw new SOSHibernateSessionException(e);
        }
    }

    public void reopen() throws SOSHibernateException {
        String method = getMethodName("reopen");
        LOGGER.debug(String.format("%s: isStatelessSession = %s", method, isStatelessSession));
        closeSession();
        openSession();
    }

    /** @deprecated
     * 
     *             use close(); */
    @Deprecated
    public void disconnect() {
        close();
    }

    public void close() {
        String method = getMethodName("close");
        LOGGER.debug(String.format("%s", method));
        closeTransaction();
        closeSession();
    }

    public void beginTransaction() throws SOSHibernateException {
        String method = getMethodName("beginTransaction");
        if (getFactory().getAutoCommit()) {
            LOGGER.debug(String.format("%s: skip (autoCommit is true)", method));
            return;
        }
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new SOSHibernateSessionException("currentSession is NULL");
        }
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                session.beginTransaction();
            } else {
                Session session = ((Session) currentSession);
                session.beginTransaction();
            }
        } catch (Throwable e) {
            throw new SOSHibernateTransactionException(e);
        }
    }

    public void commit() throws SOSHibernateException {
        String method = getMethodName("commit");
        if (getFactory().getAutoCommit()) {
            LOGGER.debug(String.format("%s: skip (autoCommit is true)", method));
            return;
        }
        LOGGER.debug(String.format("%s", method));
        Transaction tr = getTransaction();
        if (tr == null) {
            throw new SOSHibernateTransactionException("session transaction is NULL");
        }
        try {
            if (!isStatelessSession) {
                ((Session) currentSession).flush();
            }
            tr.commit();
        } catch (Throwable e) {
            throw new SOSHibernateTransactionException(e);
        }
    }

    public void rollback() throws SOSHibernateException {
        String method = getMethodName("rollback");
        if (getFactory().getAutoCommit()) {
            LOGGER.debug(String.format("%s: skip (autoCommit is true)", method));
            return;
        }
        LOGGER.debug(String.format("%s", method));
        Transaction tr = getTransaction();
        if (tr == null) {
            throw new SOSHibernateTransactionException("session transaction is NULL");
        }
        try {
            tr.rollback();
        } catch (Throwable e) {
            throw new SOSHibernateTransactionException(e);
        }
    }

    public Transaction getTransaction() throws SOSHibernateException {
        Transaction tr = null;
        if (currentSession == null) {
            throw new SOSHibernateSessionException("currentSession is NULL");
        }
        try {
            if (isStatelessSession) {
                StatelessSession s = ((StatelessSession) currentSession);
                tr = s.getTransaction();
            } else {
                Session s = ((Session) currentSession);
                tr = s.getTransaction();
            }
        } catch (Throwable e) {
            throw new SOSHibernateTransactionException(e);
        }
        return tr;
    }

    public void save(Object item) throws SOSHibernateException {
        String method = getMethodName("save");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new SOSHibernateSessionException("session is NULL");
        }
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                session.insert(item);
            } else {
                Session session = ((Session) currentSession);
                session.save(item);
                session.flush();
            }
        } catch (Throwable e) {
            throw new SOSHibernateSessionException(e);
        }
    }

    public void update(Object item) throws SOSHibernateException {
        String method = getMethodName("update");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new SOSHibernateSessionException("session is NULL");
        }
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                session.update(item);
            } else {
                Session session = ((Session) currentSession);
                session.update(item);
                session.flush();
            }
        } catch (Throwable e) {
            throw new SOSHibernateSessionException(e);
        }
    }

    public Object saveOrUpdate(Object item) throws SOSHibernateException {
        String method = getMethodName("saveOrUpdate");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new SOSHibernateSessionException("currentSession is NULL");
        }
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                /*
                 * The following error will always be logged in the try segment, if the item id field is null: SQL Error: -1, SQLState: 07004 Parameter at
                 * position 9 is not set HHH000010: On release of batch it still contained JDBC statements in a stateless session it is better to check if the
                 * item is a new entry and then call save() or an existing item and then call update() there is no need to create an error to switch the
                 * statement afterwards
                 */
                try {
                    session.update(item);
                } catch (Exception e) {
                    session.insert(item);
                }
            } else {
                Session session = ((Session) currentSession);
                session.saveOrUpdate(item);
                session.flush();
            }
        } catch (Throwable e) {
            throw new SOSHibernateSessionException(e);
        }
        return item;
    }

    public void delete(Object item) throws SOSHibernateException {
        String method = getMethodName("delete");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new SOSHibernateSessionException("currentSession is NULL");
        }
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                session.delete(item);
            } else {
                Session session = ((Session) currentSession);
                session.delete(item);
                session.flush();
            }
        } catch (Throwable e) {
            throw new SOSHibernateSessionException(e);
        }
    }

    public void refresh(Object object) throws SOSHibernateException {
        refresh(null, object);
    }

    public void refresh(String entityName, Object object) throws SOSHibernateException {
        try {
            if (isStatelessSession) {
                StatelessSession session = ((StatelessSession) currentSession);
                if (entityName == null) {
                    session.refresh(object);
                } else {
                    session.refresh(entityName, object);
                }
            } else {
                Session session = (Session) currentSession;
                if (entityName == null) {
                    session.refresh(object);
                } else {
                    session.refresh(entityName, object);
                }
            }
        } catch (Throwable e) {
            throw new SOSHibernateSessionException(e);
        }
    }

    public Object get(Class<?> entityClass, Serializable id) throws SOSHibernateException {
        try {
            if (isStatelessSession) {
                return ((StatelessSession) currentSession).get(entityClass, id);
            } else {
                return ((Session) currentSession).get(entityClass, id);
            }
        } catch (Throwable e) {
            throw new SOSHibernateSessionException(e);
        }
    }

    public <T> Query<T> createQuery(String hql) throws SOSHibernateException {
        return createQuery(hql, null);
    }

    @SuppressWarnings("unchecked")
    public <T> Query<T> createQuery(String hql, Class<T> entityClass) throws SOSHibernateException {
        String method = getMethodName("createQuery");
        LOGGER.debug(String.format("%s: hql = %s", method, hql));
        if (currentSession == null) {
            throw new SOSHibernateQueryException("session is NULL");
        }
        Query<T> q = null;
        try {
            if (isStatelessSession) {
                if (entityClass == null) {
                    q = ((StatelessSession) currentSession).createQuery(hql);
                } else {
                    q = ((StatelessSession) currentSession).createQuery(hql, entityClass);
                }
            } else {
                if (entityClass == null) {
                    q = ((Session) currentSession).createQuery(hql);
                } else {
                    q = ((Session) currentSession).createQuery(hql, entityClass);
                }
            }
        } catch (Throwable e) {
            throw new SOSHibernateQueryException(e);
        }
        return q;
    }

    /** @deprecated method for compatibility with the 1.11.0 an 1.11.1 versions use createNativeQuery */
    @Deprecated
    public SQLQuery<?> createSQLQuery(String sql) throws SOSHibernateException {
        return createSQLQuery(sql, null);
    }

    /** @deprecated method for compatibility with the 1.11.0 an 1.11.1 versions use createNativeQuery */
    @Deprecated
    public SQLQuery<?> createSQLQuery(String sql, Class<?> entityClass) throws SOSHibernateException {
        String method = getMethodName("createSQLQuery");
        LOGGER.debug(String.format("%s: sql=%s", method, sql));
        if (currentSession == null) {
            throw new SOSHibernateQueryException("currentSession is NULL");
        }
        SQLQuery<?> q = null;
        try {
            if (isStatelessSession) {
                q = ((StatelessSession) currentSession).createSQLQuery(sql);
            } else {
                q = ((Session) currentSession).createSQLQuery(sql);
            }
            if (q != null && entityClass != null) {
                q.addEntity(entityClass);
            }
        } catch (Throwable e) {
            throw new SOSHibernateQueryException(e);
        }
        return q;
    }

    public <T> NativeQuery<T> createNativeQuery(String sql) throws SOSHibernateException {
        return createNativeQuery(sql, null);
    }

    @SuppressWarnings("unchecked")
    public <T> NativeQuery<T> createNativeQuery(String sql, Class<T> entityClass) throws SOSHibernateException {
        String method = getMethodName("createNativeQuery");
        LOGGER.debug(String.format("%s: sql=%s", method, sql));
        if (currentSession == null) {
            throw new SOSHibernateQueryException("currentSession is NULL");
        }
        NativeQuery<T> q = null;
        try {
            if (isStatelessSession) {
                if (entityClass == null) {
                    q = ((StatelessSession) currentSession).createNativeQuery(sql);
                } else {
                    q = ((StatelessSession) currentSession).createNativeQuery(sql, entityClass);
                }
            } else {
                if (entityClass == null) {
                    q = ((Session) currentSession).createNativeQuery(sql);
                } else {
                    q = ((Session) currentSession).createNativeQuery(sql, entityClass);
                }
            }
        } catch (Throwable e) {
            throw new SOSHibernateQueryException(e);
        }
        return q;
    }

    public String getSingleValue(String hql) throws SOSHibernateException {
        return getSingleValue(createQuery(hql));
    }

    public String getNativeQuerySingleValue(String sql) throws SOSHibernateException {
        return getSingleValue(createNativeQuery(sql));
    }

    /** return the first possible value or null
     * 
     * difference to Query.getSingleResult - not throw NoResultException, return single value as string */
    public <T> String getSingleValue(Query<T> query) throws SOSHibernateException {
        String result = null;
        List<T> results = null;
        try {
            results = query.getResultList();
        } catch (Throwable e) {
            throw new SOSHibernateQueryException(e);
        }
        if (results != null && !results.isEmpty()) {
            if (results.get(0) instanceof Object[]) {
                Object[] obj = (Object[]) results.get(0);
                result = obj[0] + "";
            } else {
                result = results.get(0) + "";
            }
        }
        return result;
    }

    public <T> T getSingleResult(String hql) throws SOSHibernateException {
        return getSingleResult(createQuery(hql));
    }

    /** return the first possible result or null
     * 
     * difference to Query.getSingleResult - not throw NoResultException */
    public <T> T getSingleResult(Query<T> query) throws SOSHibernateException {
        T result = null;
        List<T> results = null;
        try {
            results = query.getResultList();
        } catch (Throwable e) {
            throw new SOSHibernateQueryException(e);
        }
        if (results != null && !results.isEmpty()) {
            result = results.get(0);
        }
        return result;
    }

    public <T> Map<String, String> getNativeQuerySingleResult(String sql) throws SOSHibernateException {
        return getNativeQuerySingleResult(sql, null);
    }

    public <T> Map<String, String> getNativeQuerySingleResult(String sql, String dateTimeFormat) throws SOSHibernateException {
        return getSingleResult(createNativeQuery(sql), dateTimeFormat);
    }

    public <T> Map<String, String> getSingleResult(NativeQuery<T> query) throws SOSHibernateException {
        return getSingleResult(query, null);
    }

    /** return a single row represented by Map<String,String>
     * 
     * Map - see getResultList */
    public <T> Map<String, String> getSingleResult(NativeQuery<T> query, String dateTimeFormat) throws SOSHibernateException {
        Map<String, String> result = null;
        List<Map<String, String>> resultList = getResultList(query, dateTimeFormat);
        if (resultList != null && !resultList.isEmpty()) {
            result = new HashMap<String, String>();
            Map<String, String> map = resultList.get(0);
            for (String key : map.keySet()) {
                result.put(key, map.get(key));
            }
        }
        return result;
    }

    public <T> List<Map<String, String>> getNativeQueryResultList(String sql) throws SOSHibernateException {
        return getNativeQueryResultList(sql, null);
    }

    public <T> List<Map<String, String>> getNativeQueryResultList(String sql, String dateTimeFormat) throws SOSHibernateException {
        return getResultList(createNativeQuery(sql), dateTimeFormat);
    }

    public <T> List<Map<String, String>> getResultList(NativeQuery<T> query) throws SOSHibernateException {
        return getResultList(query, null);
    }

    /** return a list of rows represented by Map<String,String>:
     * 
     * Map key - column name (lower case), Map value - value as string
     * 
     * 
     * setResultTransformer is deprecated (see below), but currently without alternative
     * 
     * excerpt from Query.setResultTransformer comment:
     * 
     * deprecated (since 5.2), todo develop a new approach to result transformers */
    @SuppressWarnings({ "deprecation", "unchecked" })
    public <T> List<Map<String, String>> getResultList(NativeQuery<T> query, String dateTimeFormat) throws SOSHibernateException {
        try {
            query.setResultTransformer(new ResultTransformer() {

                private static final long serialVersionUID = 1L;

                @Override
                public Object transformTuple(Object[] tuple, String[] aliases) {
                    Map<String, String> result = new HashMap<String, String>(tuple.length);
                    for (int i = 0; i < tuple.length; i++) {
                        String alias = aliases[i];
                        if (alias != null) {
                            Object origValue = tuple[i];
                            String value = "";
                            if (origValue != null) {
                                value = origValue + "";
                                if (origValue instanceof java.sql.Timestamp && dateTimeFormat != null) {
                                    try {
                                        value = SOSDate.getDateTimeAsString(value, dateTimeFormat);
                                    } catch (Exception e) {
                                    }
                                }
                            }
                            result.put(alias.toLowerCase(), value);
                        }
                    }
                    return result;
                }

                @SuppressWarnings("rawtypes")
                @Override
                public List<?> transformList(List collection) {
                    return collection;
                }
            });
            return (List<Map<String, String>>) query.getResultList();
        } catch (Throwable e) {
            throw new SOSHibernateQueryException(e);
        }
    }
    
    public <T> List<T> getResultList(Query<T> query) throws SOSHibernateException{
        try{
            return query.getResultList();
        } catch (Throwable e) {
            throw new SOSHibernateQueryException(e);
        }
    }

    @Deprecated
    public Criteria createCriteria(Class<?> cl, String alias) throws SOSHibernateException {
        String method = getMethodName("createCriteria");
        LOGGER.debug(String.format("%s: class = %s", method, cl.getSimpleName()));
        if (currentSession == null) {
            throw new SOSHibernateCriteriaException("currentSession is NULL");
        }
        Criteria cr = null;
        try {
            if (isStatelessSession) {
                cr = ((StatelessSession) currentSession).createCriteria(cl, alias);
            } else {
                cr = ((Session) currentSession).createCriteria(cl, alias);
            }
        } catch (Throwable e) {
            throw new SOSHibernateCriteriaException(e);
        }
        return cr;
    }

    @Deprecated
    public Criteria createCriteria(Class<?> cl) throws SOSHibernateException {
        return createCriteria(cl, (String) null);
    }

    @Deprecated
    public Criteria createCriteria(Class<?> cl, String[] selectProperties) throws SOSHibernateException {
        return createCriteria(cl, selectProperties, null);
    }

    @Deprecated
    public Criteria createCriteria(Class<?> cl, String[] selectProperties, ResultTransformer transformer) throws SOSHibernateException {
        Criteria cr = createCriteria(cl);
        if (cr == null) {
            throw new SOSHibernateCriteriaException("criteria is NULL");
        }
        try {
            if (selectProperties != null) {
                ProjectionList pl = Projections.projectionList();
                for (String property : selectProperties) {
                    pl.add(Projections.property(property), property);
                }
                cr.setProjection(pl);
            }
            if (transformer != null) {
                cr.setResultTransformer(transformer);
            }
        } catch (Throwable e) {
            throw new SOSHibernateCriteriaException(e);
        }
        return cr;
    }

    @Deprecated
    public Criteria createSingleListTransform2BeanCriteria(Class<?> cl, String selectProperty) throws SOSHibernateException {
        return createSingleListCriteria(cl, selectProperty, Transformers.aliasToBean(cl));
    }

    @Deprecated
    public Criteria createSingleListCriteria(Class<?> cl, String selectProperty) throws SOSHibernateException {
        return createSingleListCriteria(cl, selectProperty, null);
    }

    @Deprecated
    public Criteria createSingleListCriteria(Class<?> cl, String selectProperty, ResultTransformer transformer) throws SOSHibernateException {
        return createCriteria(cl, new String[] { selectProperty }, transformer);
    }

    @Deprecated
    public Criteria createTransform2BeanCriteria(Class<?> cl) throws Exception {
        return createCriteria(cl, null, null);
    }

    @Deprecated
    public Criteria createTransform2BeanCriteria(Class<?> cl, String[] selectProperties) throws SOSHibernateException {
        return createCriteria(cl, selectProperties, Transformers.aliasToBean(cl));
    }

    public static Criterion createInCriterion(String propertyName, List<?> list) {
        Criterion criterion = null;
        int size = list.size();

        for (int i = 0; i < size; i += LIMIT_IN_CLAUSE) {
            List<?> subList;
            if (size > i + LIMIT_IN_CLAUSE) {
                subList = list.subList(i, (i + LIMIT_IN_CLAUSE));
            } else {
                subList = list.subList(i, size);
            }
            if (criterion != null) {
                criterion = Restrictions.or(criterion, Restrictions.in(propertyName, subList));
            } else {
                criterion = Restrictions.in(propertyName, subList);
            }
        }
        return criterion;
    }

    private String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s] ", identifier);
        return String.format("%s%s", prefix, name);
    }

    private void closeTransaction() {
        String method = getMethodName("closeTransaction");
        try {
            if (currentSession != null) {
                Transaction tr = getTransaction();
                if (tr != null) {
                    LOGGER.debug(String.format("%s: rollback", method));
                    tr.rollback();
                } else {
                    LOGGER.debug(String.format("%s: skip rollback (transaction is null)", method));
                }
            }
        } catch (Throwable ex) {
            //
        }

    }

    private void closeSession() {
        String method = getMethodName("closeSession");
        LOGGER.debug(String.format("%s", method));
        try {
            if (currentSession != null) {
                if (isStatelessSession) {
                    StatelessSession s = (StatelessSession) currentSession;
                    s.close();
                } else {
                    Session session = (Session) currentSession;
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            }
        } catch (Throwable e) {
        }
        currentSession = null;
    }

}