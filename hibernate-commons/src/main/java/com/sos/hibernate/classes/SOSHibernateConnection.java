package com.sos.hibernate.classes;

import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.impl.CriteriaImpl;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.jdbc.Work;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
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

import sos.util.SOSString;

public class SOSHibernateConnection implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateConnection.class);
    private Optional<File> configFile;
    private Configuration configuration;
    private SessionFactory sessionFactory;
    private Dialect dialect;
    private Object currentSession;
    private Connection jdbcConnection;
    private boolean useOpenStatelessSession;
    private boolean useGetCurrentSession;
    private FlushMode sessionFlushMode;
    private Properties defaultConfigurationProperties;
    private Properties configurationProperties;
    private ClassList classMapping;
    private boolean useDefaultConfigurationProperties = true;
    private String connectionIdentifier;
    private Optional<Integer> jdbcFetchSize = Optional.empty();
    private Enum<SOSHibernateConnection.Dbms> dbms = Dbms.UNKNOWN;
    private boolean ignoreAutoCommitTransactions = false;
    private String openSessionMethodName;
    public static final String HIBERNATE_PROPERTY_TRANSACTION_ISOLATION = "hibernate.connection.isolation";
    public static final String HIBERNATE_PROPERTY_AUTO_COMMIT = "hibernate.connection.autocommit";
    public static final String HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET = "hibernate.jdbc.use_scrollable_resultset";
    public static final String HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";
    public static final String HIBERNATE_PROPERTY_JDBC_FETCH_SIZE = "hibernate.jdbc.fetch_size";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public SOSHibernateConnection() throws Exception {
        this(null);
    }

    public SOSHibernateConnection(String hibernateConfigFile) throws Exception {
        initConfigFile(hibernateConfigFile);
        initClassMapping();
        initConfigurationProperties();
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
            if (configuration == null) {
                initConfiguration();
            }
            initSessionFactory();
            openSession();
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()));
        }
    }

    public void connect() throws Exception {
        String method = getMethodName("connect");
        try {
            initConfiguration();
            initSessionFactory();
            openSession();
            String connFile = (configFile.isPresent()) ? configFile.get().getCanonicalPath() : "without config file";
            int isolationLevel = getTransactionIsolation();
            LOGGER.debug(String.format("%s: autocommit = %s, transaction isolation = %s, %s, %s", method, getAutoCommit(),
                    getTransactionIsolationName(isolationLevel), openSessionMethodName, connFile));
        } catch (Exception ex) {
            throw new Exception(String.format("%s: %s", method, ex.toString()));
        }
    }

    private void initConfiguration() throws Exception {
        String method = getMethodName("initConfiguration");
        LOGGER.debug(String.format("%s", method));
        configuration = new Configuration();
        setConfigurationClassMapping();
        setDefaultConfigurationProperties();
        configure();
        setConfigurationProperties();
        logConfigurationProperties();
    }

    private void configure() throws Exception {
        String method = getMethodName("configure");
        if (configFile.isPresent()) {
            LOGGER.debug(String.format("%s: configure connection with hibernate file = %s", method, configFile.get().getCanonicalPath()));
            configuration.configure(configFile.get());
        } else {
            LOGGER.debug(String.format("%s: configure connection without the hibernate file", method));
            configuration.configure();
        }
    }

    private void initSessionProperties() {
        sessionFlushMode = FlushMode.ALWAYS;
        useOpenStatelessSession = false;
        useGetCurrentSession = false;
    }

    private void openSession() {
        String method = getMethodName("openSession");
        LOGGER.debug(String.format("%s: useOpenStatelessSession = %s, useGetCurrentSession = %s", method, useOpenStatelessSession,
                useGetCurrentSession));
        openSessionMethodName = "";
        if (useOpenStatelessSession) {
            currentSession = sessionFactory.openStatelessSession(jdbcConnection);
            openSessionMethodName = "openStatelessSession";
        } else {
            Session session = null;
            if (useGetCurrentSession) {
                session = sessionFactory.getCurrentSession();
                openSessionMethodName = "getCurrentSession";
            } else {
                session = sessionFactory.openSession(jdbcConnection);
                openSessionMethodName = "openSession";
            }
            if (sessionFlushMode != null) {
                session.setFlushMode(sessionFlushMode);
            }
            currentSession = session;
        }
    }

    private void initSessionFactory() throws Exception {
        String method = getMethodName("initSessionFactory");
        LOGGER.debug(String.format("%s", method));
        if (currentSession != null) {
            disconnect();
        }
        sessionFactory = configuration.buildSessionFactory();
        SessionFactoryImpl sf = (SessionFactoryImpl) sessionFactory;
        try {
            dialect = sf.getDialect();
        } catch (Exception ex) {
            throw new Exception(String.format("%s: cannot get dialect : %s", method, ex.toString()));
        }
        try {
            jdbcConnection = sf.getConnectionProvider().getConnection();
        } catch (Exception ex) {
            throw new Exception(String.format("%s: cannot get jdbcConnection : %s", method, ex.toString()));
        }
        setDbms();
    }

    public boolean getConfiguredAutoCommit() throws Exception {
        if (configuration == null) {
            throw new Exception("configuration is NULL");
        }
        String p = configuration.getProperty(HIBERNATE_PROPERTY_AUTO_COMMIT);
        if (SOSString.isEmpty(p)) {
            throw new Exception(String.format("\"%s\" property is not configured ", HIBERNATE_PROPERTY_AUTO_COMMIT));
        }
        return Boolean.parseBoolean(p);
    }

    public int getConfiguredTransactionIsolation() throws Exception {
        if (configuration == null) {
            throw new Exception("configuration is NULL");
        }
        String p = configuration.getProperty(HIBERNATE_PROPERTY_TRANSACTION_ISOLATION);
        if (SOSString.isEmpty(p)) {
            throw new Exception(String.format("\"%s\" property is not configured ", HIBERNATE_PROPERTY_TRANSACTION_ISOLATION));
        }
        return Integer.parseInt(p);
    }

    public void setAutoCommit(boolean commit) {
        configurationProperties.put(HIBERNATE_PROPERTY_AUTO_COMMIT, String.valueOf(commit));
    }

    public void setTransactionIsolation(int level) {
        configurationProperties.put(HIBERNATE_PROPERTY_TRANSACTION_ISOLATION, String.valueOf(level));
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
            LOGGER.warn(String.format("%s: cannot set dbms. %s", method, ex.toString()));
        }
    }

    public static Throwable getException(Throwable ex) {
        if (ex instanceof SQLGrammarException) {
            SQLGrammarException sqlGrEx = (SQLGrammarException) ex;
            SQLException sqlEx = sqlGrEx.getSQLException();
            return new Exception(String.format("%s [exception: %s, sql: %s]", ex.getMessage(), sqlEx == null ? "" : sqlEx.getMessage(),
                    sqlGrEx.getSQL()), sqlEx);
        } else if (ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }

    public Enum<SOSHibernateConnection.Dbms> getDbms() {
        return dbms;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public void disconnect() {
        String method = getMethodName("disconnect");
        LOGGER.debug(String.format("%s", method));
        closeTransaction();
        closeSession();
        closeSessionFactory();
        closeJdbcConnection();
        dialect = null;
    }

    private void closeJdbcConnection() {
        String method = getMethodName("closeJdbcConnection");
        LOGGER.debug(String.format("%s", method));
        if (jdbcConnection != null) {
            try {
                jdbcConnection.close();
            } catch (Exception ex) {
            }
        }
        jdbcConnection = null;
    }

    public void clearSession() throws Exception {
        String method = getMethodName("clearSession");
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
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
            throw new Exception(String.format("currentSession is NULL"));
        }
        if (currentSession instanceof Session) {
            Session session = (Session) currentSession;
            session.doWork(work);
        } else {
            LOGGER.warn(String.format("%s: this method will be ignored for current openSessionMethodName : %s (%s)", method, openSessionMethodName,
                    currentSession.getClass().getSimpleName()));
        }
    }

    private void closeSession() {
        String method = getMethodName("closeSession");
        LOGGER.debug(String.format("%s", method));
        if (currentSession != null) {
            if (currentSession instanceof Session) {
                if (!useGetCurrentSession) {
                    Session session = (Session) currentSession;
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            } else if (currentSession instanceof StatelessSession) {
                StatelessSession session = (StatelessSession) currentSession;
                session.close();
            }
        }
        currentSession = null;
    }

    private void closeSessionFactory() {
        String method = getMethodName("closeSessionFactory");
        LOGGER.debug(String.format("%s", method));
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
        sessionFactory = null;
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
                    StatelessSession session = (StatelessSession) currentSession;
                    tr = session.getTransaction();
                }
                if (tr != null) {
                    tr.rollback();
                }
            }
        } catch (Exception ex) {
        }

    }

    private void initClassMapping() {
        classMapping = new ClassList();
    }

    private void initConfigurationProperties() {
        defaultConfigurationProperties = new Properties();
        defaultConfigurationProperties.put(HIBERNATE_PROPERTY_TRANSACTION_ISOLATION, String.valueOf(Connection.TRANSACTION_READ_COMMITTED));
        defaultConfigurationProperties.put(HIBERNATE_PROPERTY_AUTO_COMMIT, "false");
        defaultConfigurationProperties.put(HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET, "true");
        defaultConfigurationProperties.put(HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS, "jta");
        configurationProperties = new Properties();
    }

    private void initConfigFile(String hibernateConfigFile) throws Exception {
        File file = null;
        if (hibernateConfigFile != null) {
            file = new File(hibernateConfigFile);
            if (!file.exists()) {
                throw new Exception(String.format("hibernate config file not found: %s", file.getCanonicalPath()));
            }
        }
        configFile = Optional.of(file);
    }

    private void logConfigurationProperties() {
        String method = getMethodName("logConfigurationProperties");
        for (Map.Entry<?, ?> entry : configuration.getProperties().entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            LOGGER.debug(String.format("%s: property: %s = %s", method, key, value));
        }
    }

    private void setConfigurationClassMapping() {
        String method = getMethodName("setConfigurationClassMapping");
        if (classMapping != null) {
            for (Class<?> c : classMapping.getClasses()) {
                configuration.addAnnotatedClass(c);
                LOGGER.debug(String.format("%s: mapping. class = %s", method, c.getCanonicalName()));
            }
        }
    }

    private void setDefaultConfigurationProperties() {
        String method = getMethodName("setDefaultConfigurationProperties");
        if (useDefaultConfigurationProperties && defaultConfigurationProperties != null) {
            for (Map.Entry<?, ?> entry : defaultConfigurationProperties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                configuration.setProperty(key, value);
                LOGGER.debug(String.format("%s: default properties. property: %s = %s", method, key, value));
            }
        }
    }

    private void setConfigurationProperties() {
        String method = getMethodName("setConfigurationProperties");
        if (configurationProperties != null) {
            for (Map.Entry<?, ?> entry : configurationProperties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                configuration.setProperty(key, value);
                LOGGER.debug(String.format("%s: custom properties. property: %s = %s", method, key, value));
            }
            if (configuration.getProperty(HIBERNATE_PROPERTY_JDBC_FETCH_SIZE) != null) {
                try {
                    jdbcFetchSize = Optional.of(Integer.parseInt(configuration.getProperty(HIBERNATE_PROPERTY_JDBC_FETCH_SIZE)));
                } catch (Exception ex) {
                }
            }
        }
    }

    public Query createQuery(String query) throws Exception {
        String method = getMethodName("createQuery");
        LOGGER.debug(String.format("%s: query = %s", method, query));
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
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
            throw new Exception(String.format("currentSession is NULL"));
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
            throw new Exception(String.format("currentSession is NULL"));
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
        if (ignoreAutoCommitTransactions && this.getAutoCommit()) {
            return;
        }
        String method = getMethodName("beginTransaction");
        LOGGER.debug(String.format("%s", method));
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
        }
        if (currentSession instanceof Session) {
            Session session = ((Session) currentSession);
            session.beginTransaction();
        } else if (currentSession instanceof StatelessSession) {
            StatelessSession session = ((StatelessSession) currentSession);
            session.beginTransaction();
        }
    }

    public void commit() throws Exception {
        if (ignoreAutoCommitTransactions && this.getAutoCommit()) {
            return;
        }
        String method = getMethodName("commit");
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
        }
        if (currentSession instanceof Session) {
            LOGGER.debug(String.format("%s", method));
            Session session = ((Session) currentSession);
            Transaction tr = session.getTransaction();
            if (tr == null) {
                throw new Exception(String.format("session transaction is NULL"));
            }
            session.flush();
            tr.commit();
        } else if (currentSession instanceof StatelessSession) {
            LOGGER.debug(String.format("%s", method));
            StatelessSession session = ((StatelessSession) currentSession);
            Transaction tr = session.getTransaction();
            if (tr == null) {
                throw new Exception(String.format("stateless session transaction is NULL"));
            }
            tr.commit();
        }
    }

    public void rollback() throws Exception {
        if (ignoreAutoCommitTransactions && this.getAutoCommit()) {
            return;
        }
        String method = getMethodName("rollback");
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
        }
        if (currentSession instanceof Session) {
            LOGGER.debug(String.format("%s", method));
            Session session = ((Session) currentSession);
            Transaction tr = session.getTransaction();
            if (tr == null) {
                throw new Exception(String.format("session transaction is NULL"));
            }
            tr.rollback();
        } else if (currentSession instanceof StatelessSession) {
            LOGGER.debug(String.format("%s", method));
            StatelessSession session = ((StatelessSession) currentSession);
            Transaction tr = session.getTransaction();
            if (tr == null) {
                throw new Exception(String.format("stateless session transaction is NULL"));
            }
            tr.rollback();
        }
    }

    public void save(Object item) throws Exception {
        String method = getMethodName("save");
        LOGGER.debug(String.format("%s: item = %s", method, item));
        if (currentSession == null) {
            throw new Exception(String.format("currentSession is NULL"));
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
            throw new Exception(String.format("currentSession is NULL"));
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
            throw new Exception(String.format("currentSession is NULL"));
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
            throw new Exception(String.format("currentSession is NULL"));
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

    public void setConfigurationProperties(Properties properties) {
        if (configurationProperties.isEmpty()) {
            configurationProperties = properties;
        } else {
            if (properties != null) {
                for (Map.Entry<?, ?> entry : properties.entrySet()) {
                    String key = (String) entry.getKey();
                    String value = (String) entry.getValue();
                    configurationProperties.setProperty(key, value);
                }
            }
        }
    }

    public void addClassMapping(ClassList list) {
        for (Class<?> c : list.getClasses()) {
            classMapping.add(c);
        }
    }

    public Optional<File> getConfigFile() {
        return configFile;
    }

    public Object getCurrentSession() throws Exception {
        return currentSession;
    }

    public boolean isUseDefaultConfigurationProperties() {
        return useDefaultConfigurationProperties;
    }

    public void setUseDefaultConfigurationProperties(boolean val) {
        useDefaultConfigurationProperties = val;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Properties getConfigurationProperties() {
        return configurationProperties;
    }

    public ClassList getClassMapping() {
        return classMapping;
    }

    public Properties getDefaultConfigurationProperties() {
        return defaultConfigurationProperties;
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

    public String getSqlStringFromCriteria(Criteria criteria) throws Exception {
        CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
        SessionImplementor session = criteriaImpl.getSession();
        SessionFactoryImplementor factory = session.getFactory();
        CriteriaQueryTranslator translator = new CriteriaQueryTranslator(factory, criteriaImpl, criteriaImpl.getEntityOrClassName(),
                CriteriaQueryTranslator.ROOT_SQL_ALIAS);
        String[] implementors = factory.getImplementors(criteriaImpl.getEntityOrClassName());
        CriteriaJoinWalker walker = new CriteriaJoinWalker((OuterJoinLoadable) factory.getEntityPersister(implementors[0]), translator, factory, criteriaImpl,
                criteriaImpl.getEntityOrClassName(), session.getLoadQueryInfluencers());
        return walker.getSQLString();
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
        return ignoreAutoCommitTransactions;
    }

    public void setIgnoreAutoCommitTransactions(boolean val) {
        ignoreAutoCommitTransactions = val;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Optional<Integer> getJdbcFetchSize() {
        return jdbcFetchSize;
    }

}