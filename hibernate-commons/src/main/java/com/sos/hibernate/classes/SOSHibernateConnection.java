package com.sos.hibernate.classes;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.Dialect;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.internal.SessionImpl;
import org.hibernate.internal.StatelessSessionImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.NumericBooleanType;
import org.hibernate.type.StringType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.exception.DBSessionException;

public class SOSHibernateConnection implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateConnection.class);
    private SOSHibernateFactory factory;
    private SessionFactory sessionFactory;
    private Dialect dialect;
    private Object currentSession;
    private Connection jdbcConnection;
    private boolean useOpenStatelessSession;
    private boolean useGetCurrentSession;
    private FlushMode sessionFlushMode;
    private String connectionIdentifier;
    private Optional<Integer> jdbcFetchSize = Optional.empty();
    private Enum<SOSHibernateConnection.Dbms> dbms = Dbms.UNKNOWN;
    private String openSessionMethodName;
    public static final String HIBERNATE_PROPERTY_TRANSACTION_ISOLATION = "hibernate.connection.isolation";
    public static final String HIBERNATE_PROPERTY_AUTO_COMMIT = "hibernate.connection.autocommit";
    public static final String HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET = "hibernate.jdbc.use_scrollable_resultset";
    public static final String HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";
    public static final String HIBERNATE_PROPERTY_JDBC_FETCH_SIZE = "hibernate.jdbc.fetch_size";
    public static final int LIMIT_IN_CLAUSE = 1000;
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public SOSHibernateConnection(SOSHibernateFactory hibernateFactory) {
        this.factory = hibernateFactory;
        this.sessionFactory = hibernateFactory.getSessionFactory();
        initSessionProperties();
    }

    public enum Dbms {
        UNKNOWN, DB2, FBSQL, MSSQL, MYSQL, ORACLE, PGSQL, SYBASE
    }

    public void reconnect() throws Exception {
        String method = getMethodName("reconnect");
        try {
            LOGGER.info(String.format("%s: useOpenStatelessSession = %s", method, useOpenStatelessSession));
            disconnect();
            openSession();
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    public void connect() throws Exception {
        String method = getMethodName("connect");
        try {
            openSession();
            String connFile = (factory.getConfigFile().isPresent()) ? factory.getConfigFile().get().getCanonicalPath() : "without config file";
            int isolationLevel = getTransactionIsolation();
            LOGGER.debug(String.format("%s: autocommit = %s, transaction isolation = %s, %s, %s", method, getAutoCommit(), getTransactionIsolationName(isolationLevel),
                    openSessionMethodName, connFile));
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()), ex);
        }
    }

    private void initSessionProperties() {
        sessionFlushMode = FlushMode.ALWAYS;
        useOpenStatelessSession = false;
        useGetCurrentSession = false;
    }

    private void openSession() throws Exception {
        String method = getMethodName("openSession");
        LOGGER.debug(String.format("%s: useOpenStatelessSession = %s, useGetCurrentSession = %s", method, useOpenStatelessSession, useGetCurrentSession));
        openSessionMethodName = "";

        if (currentSession != null) {
            if (currentSession instanceof Session) {
                ((Session) currentSession).close();
            } else {
                ((StatelessSession) currentSession).close();
            }
        }
        if (useOpenStatelessSession) {
            currentSession = sessionFactory.openStatelessSession();
            openSessionMethodName = "openStatelessSession";
        } else {
            Session session = null;
            if (useGetCurrentSession && session != null) {
                session = sessionFactory.getCurrentSession();
                openSessionMethodName = "getCurrentSession";
            } else {
                session = sessionFactory.openSession();
                openSessionMethodName = "openSession";
            }
            if (sessionFlushMode != null) {
                session.setHibernateFlushMode(sessionFlushMode);
            }
            currentSession = session;
        }
        if (currentSession instanceof Session) {
            SessionImpl sf = (SessionImpl) currentSession;
            try {
                dialect = sf.getJdbcServices().getDialect();
            } catch (Exception ex) {
                throw new Exception(String.format("%s: cannot get dialect : %s", method, ex.toString()), ex);
            }
            try {
                jdbcConnection = sf.connection();
            } catch (Exception ex) {
                throw new Exception(String.format("%s: cannot get jdbcConnection : %s", method, ex.toString()), ex);
            }
        } else {
            StatelessSessionImpl sf = (StatelessSessionImpl) currentSession;
            try {
                dialect = sf.getJdbcServices().getDialect();
            } catch (Exception ex) {
                throw new Exception(String.format("%s: cannot get dialect : %s", method, ex.toString()), ex);
            }
            try {
                jdbcConnection = sf.connection();
            } catch (Exception ex) {
                throw new Exception(String.format("%s: cannot get jdbcConnection : %s", method, ex.toString()), ex);
            }
        }
        setDbms();

    }

    public Session createSession() {
        String method = getMethodName("createSession");
        LOGGER.debug(String.format("%s: createSession", method));
        return sessionFactory.openSession();
    }

    public StatelessSession createStatelessSession() {
        String method = getMethodName("createStatelessSession");
        LOGGER.debug(String.format("%s: createStatelessSession", method));
        return sessionFactory.openStatelessSession();
    }

    public boolean getAutoCommit() throws Exception {
        if (jdbcConnection == null) {
            throw new Exception("jdbcConnection is NULL");
        }
        return jdbcConnection.getAutoCommit();
    }

    public int getTransactionIsolation() throws Exception {
        if (jdbcConnection == null) {
            throw new Exception("jdbcConnection is NULL");
        }
        return jdbcConnection.getTransactionIsolation();
    }

    public static String getTransactionIsolationName(int isolationLevel) throws Exception {
        switch (isolationLevel) {
        case Connection.TRANSACTION_NONE:
            return "TRANSACTION_NONE";
        case Connection.TRANSACTION_READ_UNCOMMITTED:
            return "TRANSACTION_READ_UNCOMMITTED";
        case Connection.TRANSACTION_READ_COMMITTED:
            return "TRANSACTION_READ_COMMITTED";
        case Connection.TRANSACTION_REPEATABLE_READ:
            return "TRANSACTION_REPEATABLE_READ";
        case Connection.TRANSACTION_SERIALIZABLE:
            return "TRANSACTION_SERIALIZABLE";
        default:
            throw new Exception(String.format("Invalid transaction isolation level = %s.", isolationLevel));
        }
    }

    private void setDbms() {
        dbms = Dbms.UNKNOWN;
        setDbmsFromJdbcConnection();
        if (dbms.equals(Dbms.UNKNOWN)) {
            setDbmsFromDialect();
        }
    }

    private void setDbmsFromDialect() {
        if (dialect != null) {
            String dialectClassName = dialect.getClass().getSimpleName().toLowerCase();
            if (dialectClassName.contains("db2")) {
                dbms = Dbms.DB2;
            } else if (dialectClassName.contains("firebird")) {
                dbms = Dbms.FBSQL;
            } else if (dialectClassName.contains("sqlserver")) {
                dbms = Dbms.MSSQL;
            } else if (dialectClassName.contains("mysql")) {
                dbms = Dbms.MYSQL;
            } else if (dialectClassName.contains("oracle")) {
                dbms = Dbms.ORACLE;
            } else if (dialectClassName.contains("postgre")) {
                dbms = Dbms.PGSQL;
            } else if (dialectClassName.contains("sybase")) {
                dbms = Dbms.SYBASE;
            }
        }
    }

    private void setDbmsFromJdbcConnection() {
        String method = "setDbmsFromJdbcConnection";
        try {
            if (jdbcConnection != null) {
                String pn = jdbcConnection.getMetaData().getDatabaseProductName();
                if (pn != null) {
                    pn = pn.toLowerCase();
                    if (pn.contains("db2")) {
                        dbms = Dbms.DB2;
                    } else if (pn.contains("firebird")) {
                        dbms = Dbms.FBSQL;
                    } else if (pn.contains("sql server")) {
                        dbms = Dbms.MSSQL;
                    } else if (pn.contains("mysql")) {
                        dbms = Dbms.MYSQL;
                    } else if (pn.contains("oracle")) {
                        dbms = Dbms.ORACLE;
                    } else if (pn.contains("postgre")) {
                        dbms = Dbms.PGSQL;
                    } else if (pn.contains("ase")) {
                        dbms = Dbms.SYBASE;
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.warn(String.format("%s: cannot set dbms. %s", method, ex.toString()), ex);
        }
    }

    public static Throwable getException(Throwable ex) {
        if (ex instanceof SQLGrammarException) {
            SQLGrammarException sqlGrEx = (SQLGrammarException) ex;
            SQLException sqlEx = sqlGrEx.getSQLException();
            return new Exception(String.format("%s [exception: %s, sql: %s]", ex.getMessage(), sqlEx == null ? "" : sqlEx.getMessage(), sqlGrEx.getSQL()), sqlEx);
        } else if (ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }

    public Enum<SOSHibernateConnection.Dbms> getDbms() {
        return dbms;
    }

    public boolean dbmsIsPostgres() {
        return dbms == SOSHibernateConnection.Dbms.PGSQL;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public void disconnect() {
        String method = getMethodName("disconnect");
        LOGGER.debug(String.format("%s", method));
        closeTransaction();
        closeSession();
    }

   
    public void clearSession() throws Exception {
        String method = getMethodName("clearSession");
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new DBSessionException("session is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.clear();
        }
    }

    public void sessionDoWork(Work work) throws Exception {
        String method = getMethodName("sessionDoWork");
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.doWork(work);
        } else {
            LOGGER.warn(String.format("%s: this method will be ignored for current openSessionMethodName : %s (%s)", method, openSessionMethodName, currentSession.getClass()
                    .getSimpleName()));
        }
    }

    public void closeSession() {
        String method = getMethodName("closeSession");
        LOGGER.debug(String.format("%s", method));
        if (currentSession != null) {
            if (currentSession instanceof Session) {
                Session session = (Session) currentSession;
                if (session.isOpen()) {
                    session.close();
                }
            } else if (currentSession instanceof StatelessSession) {
                StatelessSession s = (StatelessSession) currentSession;
                s.close();
            }
        }
        currentSession = null;
        openSessionMethodName = null;
        dialect = null;
        jdbcConnection = null;
    }

    private void closeTransaction() {
        String method = getMethodName("closeTransaction");
        LOGGER.debug(String.format("%s", method));
        try {
            if (currentSession != null) {
                Transaction tr = null;
                if (currentSession instanceof Session) {
                    Session session = (Session) currentSession;
                    tr = session.getTransaction();
                } else if (currentSession instanceof StatelessSession) {
                    StatelessSession s = (StatelessSession) currentSession;
                    tr = s.getTransaction();
                }
                if (tr != null) {
                    tr.rollback();
                }
            }
        } catch (Exception ex) {
            //
        }

    }

    public Query createQuery(String query) throws Exception {
        String method = getMethodName("createQuery");
        LOGGER.debug(String.format("%s: query = %s", method, query));
        if (currentSession == null) {
            throw new DBSessionException("Session is NULL");
        }
        Query q = null;
        if (currentSession instanceof Session) {
            q = ((Session) currentSession).createQuery(query);
        } else if (currentSession instanceof StatelessSession) {
            q = ((StatelessSession) currentSession).createQuery(query);
        }
        return q;
    }

    public SQLQuery createSQLQuery(String query) throws Exception {
        return createSQLQuery(query, null);
    }

    public SQLQuery createSQLQuery(String query, Class<?> entityClass) throws Exception {
        String method = getMethodName("createSQLQuery");
        LOGGER.debug(String.format("%s: query = %s", method, query));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        SQLQuery q = null;
        if (currentSession instanceof Session) {
            q = ((Session) currentSession).createSQLQuery(query);
        } else if (currentSession instanceof StatelessSession) {
            q = ((StatelessSession) currentSession).createSQLQuery(query);
        }
        if (q != null && entityClass != null) {
            q.addEntity(entityClass);
        }
        return q;
    }

    public Criteria createCriteria(Class<?> cl, String alias) throws Exception {
        String method = getMethodName("createCriteria");
        LOGGER.debug(String.format("%s: class = %s", method, cl.getSimpleName()));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        Criteria cr = null;
        if (currentSession instanceof Session) {
            cr = ((Session) currentSession).createCriteria(cl, alias);
        } else if (currentSession instanceof StatelessSession) {
            cr = ((StatelessSession) currentSession).createCriteria(cl, alias);
        }
        return cr;
    }

    public Criteria createCriteria(Class<?> cl) throws Exception {
        return createCriteria(cl, (String) null);
    }

    public Criteria createCriteria(Class<?> cl, String[] selectProperties) throws Exception {
        return createCriteria(cl, selectProperties, null);
    }

    public Criteria createCriteria(Class<?> cl, String[] selectProperties, ResultTransformer transformer) throws Exception {
        Criteria cr = createCriteria(cl);
        if (cr == null) {
            throw new Exception("Criteria is NULL");
        }
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
        return cr;
    }

    public Criteria createSingleListTransform2BeanCriteria(Class<?> cl, String selectProperty) throws Exception {
        return createSingleListCriteria(cl, selectProperty, Transformers.aliasToBean(cl));
    }

    public Criteria createSingleListCriteria(Class<?> cl, String selectProperty) throws Exception {
        return createSingleListCriteria(cl, selectProperty, null);
    }

    public Criteria createSingleListCriteria(Class<?> cl, String selectProperty, ResultTransformer transformer) throws Exception {
        return createCriteria(cl, new String[] { selectProperty }, transformer);
    }

    public Criteria createTransform2BeanCriteria(Class<?> cl) throws Exception {
        return createCriteria(cl, null, null);
    }

    public Criteria createTransform2BeanCriteria(Class<?> cl, String[] selectProperties) throws Exception {
        return createCriteria(cl, selectProperties, Transformers.aliasToBean(cl));
    }

    public void beginTransaction() throws Exception {
        if (factory.isIgnoreAutoCommitTransactions() && this.getAutoCommit()) {
         //   return;
        }
        String method = getMethodName("beginTransaction");
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.beginTransaction();
        } else if (currentSession instanceof StatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            session.beginTransaction();
        }
    }

    public Transaction getTransaction() throws Exception {
        return getTransaction(currentSession);
    }

    public Transaction getTransaction(Object session) throws Exception {
        Transaction tr = null;
        if (session == null) {
            throw new DBSessionException("session is NULL");
        }
        if (session instanceof Session) {
            Session s = ((Session) session);
            tr = s.getTransaction();
        } else if (session instanceof StatelessSession) {
            StatelessSession s = ((StatelessSession) session);
            tr = s.getTransaction();
        }
        return tr;
    }

    public void commit() throws Exception {
        String method = getMethodName("commit");
        LOGGER.debug(String.format("%s", method));

        if (factory.isIgnoreAutoCommitTransactions() && this.getAutoCommit()) {
            return;
        }
        Transaction tr = getTransaction(currentSession);
        if (tr == null) {
            throw new Exception(String.format("session transaction is NULL"));
        }
        if (currentSession instanceof Session) {
            ((Session) currentSession).flush();
        }
        tr.commit();
    }

    public void rollback() throws Exception {
        String method = getMethodName("rollback");
        LOGGER.debug(String.format("%s", method));
        if (factory.isIgnoreAutoCommitTransactions() && this.getAutoCommit()) {
            return;
        }
        Transaction tr = getTransaction(currentSession);
        if (tr == null) {
            throw new Exception(String.format("session transaction is NULL"));
        }
        tr.rollback();
    }

    public void save(Object item) throws Exception {
        String method = getMethodName("save");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new DBSessionException("session is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.save(item);
            session.flush();
        } else if (currentSession instanceof StatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            session.insert(item);
        }
    }

    public void update(Object item) throws Exception {
        String method = getMethodName("update");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new DBSessionException("session is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.update(item);
            session.flush();
        } else if (currentSession instanceof StatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            session.update(item);
        }
    }

    public Object saveOrUpdate(Object item) throws Exception {
        String method = getMethodName("saveOrUpdate");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.saveOrUpdate(item);
            session.flush();
        } else if (currentSession instanceof StatelessSession) {
            throw new Exception(String.format("saveOrUpdate method is not allowed for this session instance: %s", currentSession.toString()));
        }
        return item;
    }

    public void delete(Object item) throws Exception {
        String method = getMethodName("delete");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new DBSessionException("currentSession is NULL");
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.delete(item);
            session.flush();
        } else if (currentSession instanceof StatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            session.delete(item);
        }
    }

    public Object getCurrentSession() throws Exception {
        return currentSession;
    }

    public Configuration getConfiguration() {
        return factory.getConfiguration();
    }

    public Connection getJdbcConnection() {
        return jdbcConnection;
    }

    public PreparedStatement jdbcConnectionPrepareStatement(String sql) throws Exception {
        if (jdbcConnection == null) {
            throw new Exception("jdbcConnection is NULL");
        }
        return jdbcConnection.prepareStatement(sql);
    }

    public FlushMode getSessionFlushMode() {
        return sessionFlushMode;
    }

    public void setSessionFlushMode(FlushMode sessionFlushMode) {
        this.sessionFlushMode = sessionFlushMode;
    }

    private String getMethodName(String name) {
        String prefix = connectionIdentifier == null ? "" : String.format("[%s] ", connectionIdentifier);
        return String.format("%s%s", prefix, name);
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

  

    public String quote(Type type, Object value) throws Exception {
        if (dialect == null) {
            throw new Exception("dialect is NULL");
        }
        if (value == null) {
            return "NULL";
        }
        if (type instanceof org.hibernate.type.NumericBooleanType) {
            return NumericBooleanType.INSTANCE.objectToSQLString((Boolean) value, dialect);
        } else if (type instanceof org.hibernate.type.LongType) {
            return org.hibernate.type.LongType.INSTANCE.objectToSQLString((Long) value, dialect);
        } else if (type instanceof org.hibernate.type.StringType) {
            return StringType.INSTANCE.objectToSQLString((String) value, dialect);
        } else if (type instanceof org.hibernate.type.TimestampType) {
            if (dbms.equals(Dbms.ORACLE)) {
                String val = SOSHibernateConnection.getDateAsString((Date) value);
                return "to_date('" + val + "','yyyy-mm-dd HH24:MI:SS')";
            } else if (dbms.equals(Dbms.MSSQL)) {
                String val = SOSHibernateConnection.getDateAsString((Date) value);
                return "'" + val.replace(" ", "T") + "'";
            } else {
                return TimestampType.INSTANCE.objectToSQLString((Date) value, dialect);
            }
        }
        return null;
    }

    public static String getDateAsString(Date d) throws Exception {
        DateTimeFormatter f = DateTimeFormat.forPattern(DATETIME_FORMAT);
        DateTime dt = new DateTime(d);
        return f.print(dt);
    }

    public String quoteFieldName(String columnName) {
        if (dialect != null && columnName != null) {
            String[] arr = columnName.split("\\.");
            if (arr.length == 1) {
                columnName = dialect.openQuote() + columnName + dialect.closeQuote();
            } else {
                StringBuilder sb = new StringBuilder();
                String cn = arr[arr.length - 1];
                for (int i = 0; i < arr.length - 1; i++) {
                    sb.append(arr[i] + ".");
                }
                sb.append(dialect.openQuote() + cn + dialect.closeQuote());
                columnName = sb.toString();
            }
        }
        return columnName;
    }

    public Object get(Class<?> entityClass, Serializable id) throws Exception {
        if (currentSession instanceof Session) {
            return ((Session) currentSession).get(entityClass, id);
        } else if (currentSession instanceof StatelessSession) {
            return ((StatelessSession) currentSession).get(entityClass, id);
        }
        return null;
    }

    public boolean isUseOpenStatelessSession() {
        return useOpenStatelessSession;
    }

    public void setUseOpenStatelessSession(boolean useOpenStatelessSession) {
        this.useOpenStatelessSession = useOpenStatelessSession;
    }

    public boolean isUseGetCurrentSession() {
        return useGetCurrentSession;
    }

    public void setUseGetCurrentSession(boolean useGetCurrentSession) {
        this.useGetCurrentSession = useGetCurrentSession;
    }

    public String getConnectionIdentifier() {
        return connectionIdentifier;
    }

    public void setConnectionIdentifier(String val) {
        connectionIdentifier = val;
    }

    public boolean isIgnoreAutoCommitTransactions() {
        return factory.isIgnoreAutoCommitTransactions();
    }

    public Optional<Integer> getJdbcFetchSize() {
        return jdbcFetchSize;
    }

    public SOSHibernateFactory getFactory(){
    	return this.factory;
    }
}