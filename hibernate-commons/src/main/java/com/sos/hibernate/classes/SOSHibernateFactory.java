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
import com.sos.keepass.SOSKeePassResolver;

import sos.util.SOSDate;
import sos.util.SOSString;

public class SOSHibernateFactory implements Serializable {

    public enum Dbms {
        DB2, FBSQL, MSSQL, MYSQL, ORACLE, PGSQL, SYBASE, UNKNOWN
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateFactory.class);
    private static final long serialVersionUID = 1L;
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();
    private ClassList classMapping;
    private Optional<Path> configFile = Optional.empty();
    private Configuration configuration;
    private Properties configurationProperties;
    private Enum<SOSHibernateFactory.Dbms> dbms = Dbms.UNKNOWN;
    private Properties defaultConfigurationProperties;
    private Dialect dialect;
    private String identifier;
    private String logIdentifier;
    private Optional<Integer> jdbcFetchSize = Optional.empty();
    private SessionFactory sessionFactory;
    private boolean useDefaultConfigurationProperties = true;

    public SOSHibernateFactory() throws SOSHibernateConfigurationException {
        this((String) null);
    }

    public SOSHibernateFactory(Path hibernateConfigFile) throws SOSHibernateConfigurationException {
        setIdentifier(null);
        setConfigFile(hibernateConfigFile);
        initClassMapping();
        initConfigurationProperties();
    }

    public SOSHibernateFactory(String hibernateConfigFile) throws SOSHibernateConfigurationException {
        setIdentifier(null);
        setConfigFile(hibernateConfigFile);
        initClassMapping();
        initConfigurationProperties();
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
            throw new SOSHibernateConfigurationException(String.format("invalid transaction isolation level=%s", isolationLevel));
        }
    }

    public void addClassMapping(Class<?> c) {
        classMapping.add(c);
    }

    public void addClassMapping(ClassList list) {
        for (Class<?> c : list.getClasses()) {
            classMapping.add(c);
        }
    }

    public void build() throws SOSHibernateFactoryBuildException {
        try {
            initConfiguration();
            initSessionFactory();
            if (isDebugEnabled) {
                String method = SOSHibernate.getMethodName(logIdentifier, "build");
                int isolationLevel = getTransactionIsolation();
                LOGGER.debug(String.format("%s autoCommit=%s, transactionIsolation=%s", method, getAutoCommit(), getTransactionIsolationName(
                        isolationLevel)));
            }
        } catch (SOSHibernateConfigurationException ex) {
            throw new SOSHibernateFactoryBuildException(ex, configFile);
        } catch (PersistenceException ex) {
            throw new SOSHibernateFactoryBuildException(ex);
        }
    }

    public void close() {
        if (isDebugEnabled) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "close"));
        }
        try {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
        } catch (Throwable e) {
            LOGGER.warn(e.toString(), e);
        }
        sessionFactory = null;
    }

    public boolean dbmsIsPostgres() {
        return dbms == SOSHibernateFactory.Dbms.PGSQL;
    }

    public boolean getAutoCommit() throws SOSHibernateConfigurationException {
        if (configuration == null) {
            throw new SOSHibernateConfigurationException("configuration is NULL");
        }
        String p = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT);
        if (SOSString.isEmpty(p)) {
            throw new SOSHibernateConfigurationException(String.format("\"%s\" property is not configured ",
                    SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT));
        }
        return Boolean.parseBoolean(p);
    }

    public ClassList getClassMapping() {
        return classMapping;
    }

    public Optional<Path> getConfigFile() {
        return configFile;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Properties getConfigurationProperties() {
        return configurationProperties;
    }

    public SOSHibernateSession getCurrentSession() throws SOSHibernateOpenSessionException {
        return getCurrentSession(identifier);
    }

    public SOSHibernateSession getCurrentSession(String identifier) throws SOSHibernateOpenSessionException {
        SOSHibernateSession session = new SOSHibernateSession(this);
        session.setIsGetCurrentSession(true);
        session.setIdentifier(identifier);
        session.openSession();
        return session;
    }

    public Enum<SOSHibernateFactory.Dbms> getDbms() {
        return dbms;
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

    public Properties getDefaultConfigurationProperties() {
        return defaultConfigurationProperties;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Optional<Integer> getJdbcFetchSize() {
        return jdbcFetchSize;
    }

    /** Hibernate Dialect does not provide the functions to identify the last inserted sequence value.
     * 
     * only for the next value:
     * 
     * e.g. dialiect.getSelectSequenceNextValString(sequenceName),
     * 
     * dialect.getSequenceNextValString(sequenceName) */
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

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public int getTransactionIsolation() throws SOSHibernateConfigurationException {
        if (configuration == null) {
            throw new SOSHibernateConfigurationException("configuration is NULL");
        }
        String p = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_TRANSACTION_ISOLATION);
        if (SOSString.isEmpty(p)) {
            throw new SOSHibernateConfigurationException(String.format("\"%s\" property is not configured ",
                    SOSHibernate.HIBERNATE_PROPERTY_TRANSACTION_ISOLATION));
        }
        return Integer.parseInt(p);
    }

    public boolean isUseDefaultConfigurationProperties() {
        return useDefaultConfigurationProperties;
    }

    public SOSHibernateSession openSession() throws SOSHibernateOpenSessionException {
        return openSession(identifier);
    }

    public SOSHibernateSession openSession(String identifier) throws SOSHibernateOpenSessionException {
        SOSHibernateSession session = new SOSHibernateSession(this);
        session.setIdentifier(identifier);
        session.openSession();
        return session;
    }

    public SOSHibernateSession openStatelessSession() throws SOSHibernateOpenSessionException {
        return openStatelessSession(identifier);
    }

    public SOSHibernateSession openStatelessSession(String identifier) throws SOSHibernateOpenSessionException {
        SOSHibernateSession session = new SOSHibernateSession(this);
        session.setIsStatelessSession(true);
        session.setIdentifier(identifier);
        session.openSession();
        return session;
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

    public void setAutoCommit(boolean commit) {
        configurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT, String.valueOf(commit));
    }

    public void setConfigFile(Path hibernateConfigFile) throws SOSHibernateConfigurationException {
        if (hibernateConfigFile != null) {
            if (!Files.exists(hibernateConfigFile)) {
                throw new SOSHibernateConfigurationException(String.format("hibernate config file not found: %s", hibernateConfigFile.toString()));
            }
            configFile = Optional.of(hibernateConfigFile);
        }
    }

    public void setConfigFile(String hibernateConfigFile) throws SOSHibernateConfigurationException {
        setConfigFile(hibernateConfigFile == null ? null : Paths.get(hibernateConfigFile));
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

    public void setIdentifier(String val) {
        identifier = val;
        logIdentifier = SOSHibernate.getLogIdentifier(identifier);
    }

    public void setTransactionIsolation(int level) {
        configurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_TRANSACTION_ISOLATION, String.valueOf(level));
    }

    public void setUseDefaultConfigurationProperties(boolean val) {
        useDefaultConfigurationProperties = val;
    }

    private void configure() throws SOSHibernateConfigurationException {
        try {
            if (configFile.isPresent()) {
                configuration.configure(configFile.get().toUri().toURL());
            } else {
                configuration.configure();
            }
            if (isDebugEnabled) {
                String method = SOSHibernate.getMethodName(logIdentifier, "configure");
                if (configFile.isPresent()) {
                    LOGGER.debug(String.format("%s %s", method, configFile.get().toAbsolutePath().toString()));
                } else {
                    LOGGER.debug(String.format("%s configure connection without the hibernate file", method));
                }

            }
        } catch (MalformedURLException e) {
            throw new SOSHibernateConfigurationException(String.format("exception on get configFile %s as url", configFile), e);
        } catch (PersistenceException e) {
            throw new SOSHibernateConfigurationException(e);
        }
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

    private void initClassMapping() {
        classMapping = new ClassList();
    }

    private void initConfiguration() throws SOSHibernateConfigurationException {
        if (isDebugEnabled) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "initConfiguration"));
        }
        configuration = new Configuration();
        setConfigurationClassMapping();
        setDefaultConfigurationProperties();
        configure();
        setConfigurationProperties();
        resolveCredentialStoreProperties();
        showConfigurationProperties();
    }

    private void initConfigurationProperties() {
        defaultConfigurationProperties = new Properties();
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_TRANSACTION_ISOLATION, String.valueOf(
                Connection.TRANSACTION_READ_COMMITTED));
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_AUTO_COMMIT, "false");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_USE_SCROLLABLE_RESULTSET, "true");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_CURRENT_SESSION_CONTEXT_CLASS, "jta");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_JAVAX_PERSISTENCE_VALIDATION_MODE, "none");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_PROPERTY_ID_NEW_GENERATOR_MAPPINGS, "false");
        defaultConfigurationProperties.put(SOSHibernate.HIBERNATE_SOS_PROPERTY_MSSQL_LOCK_TIMEOUT, "30000");// 30s
        configurationProperties = new Properties();
    }

    private void initSessionFactory() {
        if (isDebugEnabled) {
            LOGGER.debug(SOSHibernate.getMethodName(logIdentifier, "initSessionFactory"));
        }
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
        sessionFactory = configuration.buildSessionFactory(serviceRegistry);

        SessionFactoryImplementor impl = (SessionFactoryImplementor) sessionFactory;
        if (impl != null) {
            dialect = impl.getJdbcServices().getDialect();
            setDbms(dialect);
        }
    }

    private void showConfigurationProperties() {
        String property = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_SHOW_CONFIGURATION_PROPERTIES);
        if (property != null && property.toLowerCase().equals("true")) {
            String method = SOSHibernate.getMethodName(logIdentifier, "showConfigurationProperties");
            for (Map.Entry<?, ?> entry : configuration.getProperties().entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (key.equals(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD)) {
                    value = "***";
                }
                LOGGER.info(String.format("%s %s=%s", method, key, value));
            }
        }
    }

    private void setConfigurationClassMapping() {
        if (classMapping != null) {
            String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "setConfigurationClassMapping") : "";
            for (Class<?> c : classMapping.getClasses()) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s %s", method, c.getCanonicalName()));
                }
                configuration.addAnnotatedClass(c);
            }
        }
    }

    private void setConfigurationProperties() {
        if (configurationProperties != null) {
            String method = isDebugEnabled ? SOSHibernate.getMethodName(logIdentifier, "setConfigurationProperties") : "";
            for (Map.Entry<?, ?> entry : configurationProperties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                configuration.setProperty(key, value);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s %s=%s", method, key, value));
                }
            }
            if (configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_JDBC_FETCH_SIZE) != null) {
                try {
                    jdbcFetchSize = Optional.of(Integer.parseInt(configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_JDBC_FETCH_SIZE)));
                } catch (Exception ex) {
                    //
                }
            }
        }
    }

    private void resolveCredentialStoreProperties() throws SOSHibernateConfigurationException {
        if (configuration == null) {
            return;
        }

        try {
            String f = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_FILE);
            String kf = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_KEY_FILE);
            String p = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_PASSWORD);
            String ep = configuration.getProperty(SOSHibernate.HIBERNATE_SOS_PROPERTY_CREDENTIAL_STORE_ENTRY_PATH);
            SOSKeePassResolver r = new SOSKeePassResolver(f, kf, p);
            r.setEntryPath(ep);

            String url = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL);
            if (url != null) {
                configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_URL, r.resolve(url));
            }
            String username = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME);
            if (username != null) {
                configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_USERNAME, r.resolve(username));
            }
            String password = configuration.getProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD);
            if (password != null) {
                configuration.setProperty(SOSHibernate.HIBERNATE_PROPERTY_CONNECTION_PASSWORD, r.resolve(password));
            }

        } catch (Throwable e) {
            throw new SOSHibernateConfigurationException(e.toString(), e);
        }
    }

    private void setDbms(Dialect dialect) {
        dbms = getDbms(dialect);
    }

    private void setDefaultConfigurationProperties() {
        if (useDefaultConfigurationProperties && defaultConfigurationProperties != null) {
            String method = isTraceEnabled ? SOSHibernate.getMethodName(logIdentifier, "setDefaultConfigurationProperties") : "";
            for (Map.Entry<?, ?> entry : defaultConfigurationProperties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();
                if (isTraceEnabled) {
                    LOGGER.trace(String.format("%s %s=%s", method, key, value));
                }
                configuration.setProperty(key, value);
            }
        }
    }
}