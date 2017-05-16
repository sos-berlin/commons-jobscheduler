package com.sos.hibernate.classes;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import javax.persistence.PersistenceException;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.NumericBooleanType;
import org.hibernate.type.TimestampType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.hibernate.exceptions.SOSHibernateConfigurationException;
import com.sos.hibernate.exceptions.SOSHibernateConvertException;
import com.sos.hibernate.exceptions.SOSHibernateFactoryBuildException;
import com.sos.hibernate.exceptions.SOSHibernateOpenSessionException;

import sos.util.SOSDate;
import sos.util.SOSString;

public class SOSHibernateFactory implements Serializable {

    public static final String HIBERNATE_PROPERTY_TRANSACTION_ISOLATION = "hibernate.connection.isolation";
    public static final String HIBERNATE_PROPERTY_AUTO_COMMIT = "hibernate.connection.autocommit";
    public static final String HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET = "hibernate.jdbc.use_scrollable_resultset";
    public static final String HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";
    public static final String HIBERNATE_PROPERTY_JDBC_FETCH_SIZE = "hibernate.jdbc.fetch_size";
    public static final String HIBERNATE_PROPERTY_ID_NEW_GENERATOR_MAPPINGS = "hibernate.id.new_generator_mappings";
    public static final String HIBERNATE_PROPERTY_JAVAX_PERSISTENCE_VALIDATION_MODE = "javax.persistence.validation.mode";
    public static final int LIMIT_IN_CLAUSE = 1000;

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateFactory.class);

    private Optional<Path> configFile = Optional.empty();
    private Configuration configuration;
    private SessionFactory sessionFactory;
    private Dialect dialect;
    private Properties defaultConfigurationProperties;
    private Properties configurationProperties;
    private ClassList classMapping;
    private boolean useDefaultConfigurationProperties = true;
    private String identifier;
    private Optional<Integer> jdbcFetchSize = Optional.empty();
    private Enum<SOSHibernateFactory.Dbms> dbms = Dbms.UNKNOWN;

    public enum Dbms {
        UNKNOWN, DB2, FBSQL, MSSQL, MYSQL, ORACLE, PGSQL, SYBASE
    }

    public SOSHibernateFactory() throws SOSHibernateConfigurationException {
        this((String) null);
    }

    public SOSHibernateFactory(String hibernateConfigFile) throws SOSHibernateConfigurationException {
        setConfigFile(hibernateConfigFile);
        initClassMapping();
        initConfigurationProperties();
    }

    public SOSHibernateFactory(Path hibernateConfigFile) throws SOSHibernateConfigurationException {
        setConfigFile(hibernateConfigFile);
        initClassMapping();
        initConfigurationProperties();
    }

    public void addClassMapping(ClassList list) {
        for (Class<?> c : list.getClasses()) {
            classMapping.add(c);
        }
    }

    public void setIdentifier(String val) {
        identifier = val;
    }

    public void setAutoCommit(boolean commit) {
        configurationProperties.put(HIBERNATE_PROPERTY_AUTO_COMMIT, String.valueOf(commit));
    }

    public void setTransactionIsolation(int level) {
        configurationProperties.put(HIBERNATE_PROPERTY_TRANSACTION_ISOLATION, String.valueOf(level));
    }

    public void setConfigFile(String hibernateConfigFile) throws SOSHibernateConfigurationException {
        setConfigFile(hibernateConfigFile == null ? null : Paths.get(hibernateConfigFile));
    }

    public void setConfigFile(Path hibernateConfigFile) throws SOSHibernateConfigurationException {
        if (hibernateConfigFile != null) {
            if (!Files.exists(hibernateConfigFile)) {
                throw new SOSHibernateConfigurationException(String.format("hibernate config file not found: %s", hibernateConfigFile.toString()));
            }
            configFile = Optional.of(hibernateConfigFile);
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

    public Enum<SOSHibernateFactory.Dbms> getDbmsBeforeBuild() throws SOSHibernateConfigurationException {
        Configuration conf = new Configuration();
        Dialect dt = null;
        try {
            if (configFile.isPresent()) {
                conf.configure(configFile.get().toUri().toURL());
            } else {
                conf.configure();
            }
            dt = Dialect.getDialect(conf.getProperties());
        } catch (MalformedURLException e) {
            throw new SOSHibernateConfigurationException(String.format("exception on get configFile %s as url", configFile), e);
        } catch (PersistenceException e) {
            throw new SOSHibernateConfigurationException(e);
        }
        return getDbms(dt);
    }

    public void build() throws SOSHibernateFactoryBuildException {
        String method = getMethodName("build");
        try {
            initConfiguration();
            initSessionFactory();
            String connFile = (configFile.isPresent()) ? configFile.get().toAbsolutePath().toString() : "without config file";
            int isolationLevel = getTransactionIsolation();
            LOGGER.debug(String.format("%s: autocommit = %s, transaction isolation = %s, %s", method, getAutoCommit(), getTransactionIsolationName(
                    isolationLevel), connFile));
        } catch (SOSHibernateConfigurationException ex) {
            throw new SOSHibernateFactoryBuildException(ex, configFile);
        } catch (PersistenceException ex) {
            throw new SOSHibernateFactoryBuildException(ex);
        }
    }

    public SOSHibernateSession openSession(String identifier) throws SOSHibernateOpenSessionException {
        SOSHibernateSession session = new SOSHibernateSession(this);
        session.setIdentifier(identifier);
        session.openSession();
        return session;
    }

    public SOSHibernateSession openSession() throws SOSHibernateOpenSessionException {
        return openSession(identifier);
    }

    public SOSHibernateSession openStatelessSession(String identifier) throws SOSHibernateOpenSessionException {
        SOSHibernateSession session = new SOSHibernateSession(this);
        session.setIsStatelessSession(true);
        session.setIdentifier(identifier);
        session.openSession();
        return session;
    }

    public SOSHibernateSession openStatelessSession() throws SOSHibernateOpenSessionException {
        return openStatelessSession(identifier);
    }

    public SOSHibernateSession getCurrentSession(String identifier) throws SOSHibernateOpenSessionException {
        SOSHibernateSession session = new SOSHibernateSession(this);
        session.setIsGetCurrentSession(true);
        session.setIdentifier(identifier);
        session.openSession();
        return session;
    }

    public SOSHibernateSession getCurrentSession() throws SOSHibernateOpenSessionException {
        return getCurrentSession(identifier);
    }

    public String quote(Type type, Object value) throws SOSHibernateConvertException {
        if (value == null) {
            return "NULL";
        }
        try {
            if (type instanceof org.hibernate.type.NumericBooleanType) {
                return NumericBooleanType.INSTANCE.objectToSQLString((Boolean) value, dialect);
            } else if (type instanceof org.hibernate.type.LongType) {
                return org.hibernate.type.LongType.INSTANCE.objectToSQLString((Long) value, dialect);
            } else if (type instanceof org.hibernate.type.StringType) {
                return "'" + value.toString().replaceAll("'", "''") + "'";
            } else if (type instanceof org.hibernate.type.TimestampType) {
                if (dbms.equals(Dbms.ORACLE)) {
                    String val = SOSDate.getDateAsString((Date) value, "yyyy-MM-dd HH:mm:ss");
                    return "to_date('" + val + "','yyyy-mm-dd HH24:MI:SS')";
                } else if (dbms.equals(Dbms.MSSQL)) {
                    String val = SOSDate.getDateAsString((Date) value, "yyyy-MM-dd HH:mm:ss.SSS");
                    return "'" + val.replace(" ", "T") + "'";
                } else {
                    return TimestampType.INSTANCE.objectToSQLString((Date) value, dialect);
                }
            }
        } catch (Exception e) {
            throw new SOSHibernateConvertException(String.format("can't convert value=%s to SQL string", value), e);
        }
        return value + "";
    }

    /** @deprecated use quoteColumn instead
     * 
     *             method for compatibility with the 1.11.0 an 1.11.1 versions */
    @Deprecated
    public String quoteFieldName(String columnName) {
        return quoteColumn(columnName);
    }

    public String quoteColumn(String columnName) {
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

    public String getIdentifier() {
        return identifier;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Optional<Integer> getJdbcFetchSize() {
        return jdbcFetchSize;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public Enum<SOSHibernateFactory.Dbms> getDbms() {
        return dbms;
    }

    public boolean dbmsIsPostgres() {
        return dbms == SOSHibernateFactory.Dbms.PGSQL;
    }

    public void close() {
        String method = getMethodName("close");
        LOGGER.debug(String.format("%s", method));
        try {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("%s:%s", method, e.toString()), e);
        }
        sessionFactory = null;
    }

    public boolean getAutoCommit() throws SOSHibernateConfigurationException {
        if (configuration == null) {
            throw new SOSHibernateConfigurationException("configuration is NULL");
        }
        String p = configuration.getProperty(HIBERNATE_PROPERTY_AUTO_COMMIT);
        if (SOSString.isEmpty(p)) {
            throw new SOSHibernateConfigurationException(String.format("\"%s\" property is not configured ", HIBERNATE_PROPERTY_AUTO_COMMIT));
        }
        return Boolean.parseBoolean(p);
    }

    public int getTransactionIsolation() throws SOSHibernateConfigurationException {
        if (configuration == null) {
            throw new SOSHibernateConfigurationException("configuration is NULL");
        }
        String p = configuration.getProperty(HIBERNATE_PROPERTY_TRANSACTION_ISOLATION);
        if (SOSString.isEmpty(p)) {
            throw new SOSHibernateConfigurationException(String.format("\"%s\" property is not configured ",
                    HIBERNATE_PROPERTY_TRANSACTION_ISOLATION));
        }
        return Integer.parseInt(p);
    }

    public static String getTransactionIsolationName(int isolationLevel) throws SOSHibernateConfigurationException {
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
            throw new SOSHibernateConfigurationException(String.format("Invalid transaction isolation level = %s.", isolationLevel));
        }
    }

    /** Hibernate Dialect does not provide the functions to identify the last inserted sequence value.
     * 
     * only for the next value: e.g. dialiect.getSelectSequenceNextValString(sequenceName), dialect.getSequenceNextValString(sequenceName) */
    public String getSequenceLastValString(String sequenceName) {
        if (dbms.equals(SOSHibernateFactory.Dbms.MSSQL)) {
            return "SELECT @@IDENTITY";
        } else if (dbms.equals(SOSHibernateFactory.Dbms.MYSQL)) {
            return "SELECT LAST_INSERT_ID();";
        } else if (dbms.equals(SOSHibernateFactory.Dbms.ORACLE)) {
            return "SELECT " + sequenceName + ".currval FROM DUAL";
        } else if (dbms.equals(SOSHibernateFactory.Dbms.PGSQL)) {
            return "SELECT currval('" + sequenceName + "');";
        } else if (dbms.equals(SOSHibernateFactory.Dbms.DB2)) {
            return "SELECT IDENTITY_VAL_LOCAL() AS INSERT_ID FROM SYSIBM.SYSDUMMY1";
        } else if (dbms.equals(SOSHibernateFactory.Dbms.SYBASE)) {
            return "SELECT @@IDENTITY";
        }
        return null;
    }

    public Optional<Path> getConfigFile() {
        return configFile;
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

    private void initConfiguration() throws SOSHibernateConfigurationException {
        String method = getMethodName("initConfiguration");
        LOGGER.debug(String.format("%s", method));
        configuration = new Configuration();
        setConfigurationClassMapping();
        setDefaultConfigurationProperties();
        configure();
        setConfigurationProperties();
        logConfigurationProperties();
    }

    private void configure() throws SOSHibernateConfigurationException {
        String method = getMethodName("configure");
        try {
            if (configFile.isPresent()) {
                LOGGER.debug(String.format("%s: configure connection with hibernate file = %s", method, configFile.get().toAbsolutePath()
                        .toString()));
                configuration.configure(configFile.get().toUri().toURL());
            } else {
                LOGGER.debug(String.format("%s: configure connection without the hibernate file", method));
                configuration.configure();
            }
        } catch (MalformedURLException e) {
            throw new SOSHibernateConfigurationException(String.format("exception on get configFile %s as url", configFile), e);
        } catch (PersistenceException e) {
            throw new SOSHibernateConfigurationException(e);
        }
    }

    private void initSessionFactory() {
        String method = getMethodName("initSessionFactory");
        LOGGER.debug(String.format("%s", method));
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);

        SessionFactoryImplementor impl = (SessionFactoryImplementor) sessionFactory;
        if (impl != null) {
            dialect = impl.getJdbcServices().getDialect();
            setDbms(dialect);
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
        defaultConfigurationProperties.put(HIBERNATE_PROPERTY_JAVAX_PERSISTENCE_VALIDATION_MODE, "none");
        defaultConfigurationProperties.put(HIBERNATE_PROPERTY_ID_NEW_GENERATOR_MAPPINGS, "false");
        configurationProperties = new Properties();
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
                    //
                }
            }
        }
    }

    private void setDbms(Dialect dialect) {
        dbms = getDbms(dialect);
    }

    private Enum<SOSHibernateFactory.Dbms> getDbms(Dialect dialect) {
        SOSHibernateFactory.Dbms db = SOSHibernateFactory.Dbms.UNKNOWN;
        if (dialect != null) {
            String dialectClassName = dialect.getClass().getSimpleName().toLowerCase();
            if (dialectClassName.contains("db2")) {
                db = Dbms.DB2;
            } else if (dialectClassName.contains("firebird")) {
                db = Dbms.FBSQL;
            } else if (dialectClassName.contains("sqlserver")) {
                db = Dbms.MSSQL;
            } else if (dialectClassName.contains("mysql")) {
                db = Dbms.MYSQL;
            } else if (dialectClassName.contains("oracle")) {
                db = Dbms.ORACLE;
            } else if (dialectClassName.contains("postgre")) {
                db = Dbms.PGSQL;
            } else if (dialectClassName.contains("sybase")) {
                db = Dbms.SYBASE;
            }
        }
        return db;
    }

    public void addClassMapping(Class<?> c) {
        classMapping.add(c);
    }

    private String getMethodName(String name) {
        String prefix = identifier == null ? "" : String.format("[%s] ", identifier);
        return String.format("%s%s", prefix, name);
    }
}